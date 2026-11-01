package com.example.data.engine

import com.example.data.model.AuditReport
import com.example.data.model.VulnerabilityFinding
import com.example.data.model.VulnerabilitySeverity
import java.util.UUID

object SmartContractAuditEngine {

    fun analyze(sourceCode: String, customName: String? = null): AuditReport {
        val lines = sourceCode.lines()
        val findings = mutableListOf<VulnerabilityFinding>()

        // Extract contract name if possible
        val contractNameRegex = Regex("""contract\s+([A-Za-z0-9_]+)""")
        val detectedName = contractNameRegex.find(sourceCode)?.groupValues?.get(1) ?: (customName ?: "SmartContract")

        // Compiler version extraction
        val pragmaRegex = Regex("""pragma\s+solidity\s+([^;]+);""")
        val pragmaMatch = pragmaRegex.find(sourceCode)
        val compilerVersion = pragmaMatch?.groupValues?.get(1)?.trim() ?: "Unknown"

        // Metric counts
        val sloc = lines.count { it.trim().isNotEmpty() && !it.trim().startsWith("//") && !it.trim().startsWith("/*") }
        val functionCount = Regex("""function\s+([A-Za-z0-9_]+)""").findAll(sourceCode).count()
        val stateVarCount = lines.count { line ->
            val trimmed = line.trim()
            (trimmed.startsWith("uint") || trimmed.startsWith("int") || trimmed.startsWith("address") ||
                    trimmed.startsWith("bool") || trimmed.startsWith("mapping") || trimmed.startsWith("bytes")) &&
                    trimmed.endsWith(";") && !trimmed.contains("(")
        }

        // 1. Check Pragma
        if (pragmaMatch != null) {
            val pragmaLineNum = lines.indexOfFirst { it.contains("pragma solidity") } + 1
            val pragmaStr = pragmaMatch.groupValues[1]
            if (pragmaStr.contains("^")) {
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-103",
                        title = "Floating Pragma Detected",
                        severity = VulnerabilitySeverity.LOW,
                        lineNumber = pragmaLineNum,
                        codeSnippet = lines.getOrNull(pragmaLineNum - 1)?.trim() ?: "pragma solidity $pragmaStr;",
                        description = "Contracts should be deployed with the same compiler version they were tested with. Floating pragma (^0.8.x) can result in unexpected compilation behavior or compiler bug vulnerability.",
                        bengaliTip = "Floating pragma (^ চিহ্ণ) ব্যবহারের ফলে বিভিন্ন কম্পাইলার ভার্সনে কোড রান হতে পারে, যাতে অনাকাঙ্ক্ষিত বাগ ঢুকতে পারে। নির্দিষ্ট ভার্সন লক করুন (যেমন: pragma solidity 0.8.24;)।",
                        exploitScenario = "A newer compiler version might introduce a breaking change or regression bug that affects the compiled bytecode in production.",
                        remediation = "Lock the pragma to a specific stable compiler version, e.g., 'pragma solidity 0.8.24;'.",
                        patchedSnippet = "pragma solidity 0.8.24;"
                    )
                )
            }

            if (pragmaStr.contains("0.4.") || pragmaStr.contains("0.5.") || pragmaStr.contains("0.6.") || pragmaStr.contains("0.7.")) {
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-101",
                        title = "Outdated Compiler Version (Integer Overflow/Underflow Risk)",
                        severity = VulnerabilitySeverity.HIGH,
                        lineNumber = pragmaLineNum,
                        codeSnippet = lines.getOrNull(pragmaLineNum - 1)?.trim() ?: "pragma solidity $pragmaStr;",
                        description = "Solidity versions before 0.8.0 do not have default overflow/underflow checks. Arithmetic operations can wrap around silently without SafeMath library.",
                        bengaliTip = "Solidity 0.8.0 এর আগের ভার্সনগুলোতে ইন্টিজার ওভারফ্লো এবং আন্ডারফ্লো স্বয়ংক্রিয়ভাবে চেক হয় না। এতে ব্যালেন্স চুরি হতে পারে। সলিডিটি 0.8.20+ ব্যবহার করুন।",
                        exploitScenario = "Attacker exploits unchecked arithmetic to reset balance from 0 to 2^256-1 and withdraw all pool funds.",
                        remediation = "Upgrade to Solidity >= 0.8.0, or import and use OpenZeppelin SafeMath.",
                        patchedSnippet = "pragma solidity ^0.8.24;"
                    )
                )
            }
        }

        // 2. SWC-115: tx.origin Authorization Check
        lines.forEachIndexed { index, line ->
            if (line.contains("tx.origin") && !line.trim().startsWith("//")) {
                if (line.contains("==") || line.contains("require") || line.contains("assert") || line.contains("if")) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-115",
                            title = "Authorization Through tx.origin",
                            severity = VulnerabilitySeverity.CRITICAL,
                            lineNumber = index + 1,
                            codeSnippet = line.trim(),
                            description = "Using tx.origin for authentication or access control makes the contract vulnerable to phishing attacks. An attacker can trick the contract owner into invoking a malicious contract which forwards the call.",
                            bengaliTip = "কখনোই অথেন্টিকেশনের জন্য tx.origin ব্যবহার করবেন না! ফিশিং অ্যাটাকের মাধ্যমে মালিকের সব ফান্ড চুরি হতে পারে। সর্বদা msg.sender ব্যবহার করুন।",
                            exploitScenario = "Owner interacts with attacker's malicious contract. That contract calls this function. Since tx.origin is the owner, authorization succeeds and attacker drains funds.",
                            remediation = "Replace 'tx.origin' with 'msg.sender'.",
                            patchedSnippet = line.replace("tx.origin", "msg.sender").trim()
                        )
                    )
                }
            }
        }

        // 3. SWC-107: Reentrancy Analysis
        val hasReentrancyGuard = sourceCode.contains("ReentrancyGuard") || sourceCode.contains("nonReentrant")
        var inFunction = false
        var currentFunctionName = ""
        var functionStartLine = 0
        var foundExternalCallLine = -1
        var externalCallSnippet = ""

        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.startsWith("//") || line.startsWith("/*")) return@forEachIndexed

            if (line.startsWith("function ") || line.contains("function ")) {
                inFunction = true
                currentFunctionName = Regex("""function\s+([A-Za-z0-9_]+)""").find(line)?.groupValues?.get(1) ?: "func"
                functionStartLine = index + 1
                foundExternalCallLine = -1
            }

            // Detect external calls that transfer ETH or invoke arbitrary targets
            if (inFunction) {
                val isCallValue = line.contains(".call{value:") || line.contains(".call.value(")
                val isTransfer = line.contains(".transfer(") || line.contains(".send(")

                if (isCallValue || isTransfer) {
                    foundExternalCallLine = index + 1
                    externalCallSnippet = line
                }

                // If state modification occurs AFTER external call in same function
                if (foundExternalCallLine != -1 && (index + 1) > foundExternalCallLine) {
                    val isStateUpdate = line.contains("balances[") || line.contains("balance[") ||
                            line.contains("shares[") || line.contains("totalDeposits") ||
                            line.contains(" = 0") || line.contains(" -= ") || line.contains(" += ") ||
                            line.contains("owner =")

                    if (isStateUpdate) {
                        findings.add(
                            VulnerabilityFinding(
                                id = UUID.randomUUID().toString(),
                                swcId = "SWC-107",
                                title = "Reentrancy Vulnerability (Checks-Effects-Interactions Violation)",
                                severity = VulnerabilitySeverity.CRITICAL,
                                lineNumber = foundExternalCallLine,
                                codeSnippet = externalCallSnippet,
                                description = "External call occurs before state variables are updated (e.g. balance deduction). An attacker contract's fallback/receive function can re-enter function '$currentFunctionName' repeatedly to drain all contract Ether before the state is updated.",
                                bengaliTip = "রিয়ান্ট্র্যান্সি (Reentrancy) সবচেয়ে মারাত্মক দুর্বলতা (যেমন: TheDAO হ্যাক)। এক্সটার্নাল কলের আগেই ইউজারের ব্যালেন্স বা স্টেট আপডেট করতে হবে (Checks-Effects-Interactions প্যাটার্ন) অথবা ReentrancyGuard মডিফায়ার দিন।",
                                exploitScenario = "Attacker deposits 1 ETH, calls withdraw(). In receive(), it calls withdraw() again before balance is cleared, recursively siphoning total contract liquidity.",
                                remediation = "1. Update state variables BEFORE making external calls.\n2. Apply OpenZeppelin's ReentrancyGuard 'nonReentrant' modifier.\n3. Favor pull over push payments.",
                                patchedSnippet = "// 1. Effects: balances[msg.sender] = 0;\n// 2. Interaction: (bool success, ) = msg.sender.call{value: amount}(\"\");\n// require(success, \"Transfer failed\");"
                            )
                        )
                        foundExternalCallLine = -1
                    }
                }
            }

            if (line.endsWith("}") && inFunction && !line.contains("{")) {
                // If payable function transfers funds but has no reentrancy protection
                if (foundExternalCallLine != -1 && !hasReentrancyGuard) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-107",
                            title = "Unprotected External Call (Missing nonReentrant)",
                            severity = VulnerabilitySeverity.HIGH,
                            lineNumber = foundExternalCallLine,
                            codeSnippet = externalCallSnippet,
                            description = "Function '$currentFunctionName' makes external low-level calls without nonReentrant modifier or ReentrancyGuard protection.",
                            bengaliTip = "এই ফাংশনে এক্সটার্নাল কল হচ্ছে কিন্তু ReentrancyGuard নেই। কোনো কারণে স্টেট ডিপেন্ডেন্সি থাকলে রিয়ান্ট্র্যান্সির ঝুঁকি তৈরি হতে পারে।",
                            exploitScenario = "A malicious receiver contract can execute arbitrary code during execution.",
                            remediation = "Inherit ReentrancyGuard and add 'nonReentrant' modifier to '$currentFunctionName'.",
                            patchedSnippet = "function $currentFunctionName(...) external nonReentrant { ... }"
                        )
                    )
                }
                inFunction = false
            }
        }

        // 4. SWC-104: Unchecked Call Return Value
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if ((trimmed.contains(".call{") || trimmed.contains(".call(") || trimmed.contains(".send(")) &&
                !trimmed.startsWith("//") && !trimmed.contains("require(") && !trimmed.contains("bool success") &&
                !trimmed.contains("(bool ") && !trimmed.contains("if (")
            ) {
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-104",
                        title = "Unchecked Low-Level Call Return Value",
                        severity = VulnerabilitySeverity.HIGH,
                        lineNumber = index + 1,
                        codeSnippet = trimmed,
                        description = "The return value of a low-level call (.call or .send) is not checked. If the call fails or reverts, the contract execution continues silently without reverting, causing severe accounting errors or lost funds.",
                        bengaliTip = "Low-level .call বা .send এর রিটার্ন ভ্যালু (bool success) চেক করা হয়নি। ট্রানজেকশন ফেইল করলেও কোড রান হতে থাকবে এবং ফান্ড হারিয়ে যেতে পারে। require(success, 'Call failed'); দিন।",
                        exploitScenario = "Call to receiver fails due to gas exhaustion or custom revert, but contract still marks user as paid.",
                        remediation = "Capture return boolean: (bool success, ) = target.call{...}(\"\"); require(success, \"Call failed\");",
                        patchedSnippet = "(bool success, ) = recipient.call{value: amount}(\"\");\nrequire(success, \"Transfer failed\");"
                    )
                )
            }
        }

        // 5. SWC-106 / SWC-105: Unprotected Selfdestruct / Suicide
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if ((trimmed.contains("selfdestruct(") || trimmed.contains("suicide(")) && !trimmed.startsWith("//")) {
                val hasOwnerCheck = line.contains("onlyOwner") || sourceCode.contains("onlyOwner") || sourceCode.contains("msg.sender == owner")
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-106",
                        title = if (hasOwnerCheck) "Use of Deprecated selfdestruct" else "Unprotected / Critical selfdestruct",
                        severity = if (hasOwnerCheck) VulnerabilitySeverity.MEDIUM else VulnerabilitySeverity.CRITICAL,
                        lineNumber = index + 1,
                        codeSnippet = trimmed,
                        description = if (hasOwnerCheck) {
                            "selfdestruct is deprecated in Ethereum Cancun (EIP-6780). It no longer deletes bytecode unless called in the same transaction as contract creation."
                        } else {
                            "CRITICAL: selfdestruct can be executed by anyone! An attacker can wipe the contract and steal all remaining Ether balances."
                        },
                        bengaliTip = if (hasOwnerCheck) {
                            "Ethereum Cancun আপগ্রেডের (EIP-6780) পর selfdestruct অপপ্রচলিত (deprecated)। এটি এড়িয়ে চলুন।"
                        } else {
                            "বিপদজনক! selfdestruct-এ কোনো অ্যাক্সেস কন্ট্রোল নেই। যে কেউ এই ফাংশন ডেকে স্মার্ট কন্ট্রাক্ট ধ্বংস করে দিতে পারে!"
                        },
                        exploitScenario = "Any anonymous user calls this function, destroying contract state and forwarding ETH to attacker wallet.",
                        remediation = "Remove selfdestruct or restrict strictly to multi-sig governance with timelock.",
                        patchedSnippet = "// Deprecate selfdestruct; use pause or state flag instead."
                    )
                )
            }
        }

        // 6. SWC-116: Block Timestamp Manipulation
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("//") && (trimmed.contains("block.timestamp") || trimmed.contains("now"))) {
                if (trimmed.contains("%") || trimmed.contains("random") || trimmed.contains("winner") || trimmed.contains("lottery")) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-116",
                            title = "Weak Pseudo-Randomness via block.timestamp",
                            severity = VulnerabilitySeverity.HIGH,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "Using block.timestamp or now for randomness is easily exploitable. Validators/miners can manipulate block timestamps within ~15 seconds to game the outcome.",
                            bengaliTip = "র‍্যান্ডমনেস বা লটারি বা উইনার নির্বাচনের জন্য block.timestamp ব্যবহার করবেন না। মাইনাররা টাইমস্ট্যাম্প পরিবর্তন করে ফলাফল নিয়ন্ত্রণ করতে পারে। Chainlink VRF ব্যবহার করুন।",
                            exploitScenario = "A validator manipulates the timestamp of the winning block to ensure their own address wins the prize pool.",
                            remediation = "Use a verifiable on-chain randomness oracle such as Chainlink VRF (Verifiable Random Function).",
                            patchedSnippet = "// Use Chainlink VRF: requestRandomWords(...)"
                        )
                    )
                } else if (trimmed.contains("==") && trimmed.contains("block.timestamp")) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-116",
                            title = "Strict Equality on Block Timestamp",
                            severity = VulnerabilitySeverity.LOW,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "Checking block.timestamp == targetTime can fail because blocks are not mined at exact seconds.",
                            bengaliTip = "block.timestamp এর সাথে ঠিক সমতা (==) চেক করবেন না, কারণ নির্দিষ্ট সেকেন্ডে ব্লক মাইন না হলে কন্ডিশন কখনোই ট্রু হবে না। >= বা <= ব্যবহার করুন।",
                            exploitScenario = "Contract locks funds forever because no block was mined at that precise second.",
                            remediation = "Use inequalities (>= or <=) instead of strict equality (==).",
                            patchedSnippet = trimmed.replace("==", ">=")
                        )
                    )
                }
            }
        }

        // 7. SWC-112: Delegatecall to Untrusted Callee
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.contains(".delegatecall(") && !trimmed.startsWith("//")) {
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-112",
                        title = "Dangerous delegatecall to Untrusted Address",
                        severity = VulnerabilitySeverity.CRITICAL,
                        lineNumber = index + 1,
                        codeSnippet = trimmed,
                        description = "delegatecall executes code in the context of the calling contract. If the target address or payload can be influenced by users, the attacker can overwrite storage variables (including owner) or destroy the contract.",
                        bengaliTip = "delegatecall অত্যন্ত ঝুঁকিপূর্ণ। এটি টার্গেট কন্ট্রাক্টের কোড এই কন্ট্রাক্টের মেমরিতে রান করায়। যদি টার্গেট সুরক্ষিত না হয়, তবে হ্যাকার পুরো কন্ট্রাক্টের মালিকানা ছিনতাই করে নিতে পারে।",
                        exploitScenario = "Attacker supplies a target contract with a fallback that overwrites slot 0 (owner) to their own address, seizing complete control.",
                        remediation = "Never delegatecall to user-supplied addresses. Use immutable proxy implementations or strict whitelists.",
                        patchedSnippet = "require(trustedImplementations[target], \"Untrusted implementation\");\n(bool success, ) = target.delegatecall(data);"
                    )
                )
            }
        }

        // 8. SWC-114: Denial of Service with Failed Call / Push vs Pull
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("for ") || trimmed.startsWith("for(") || (trimmed.contains("for (") && !trimmed.startsWith("//"))) {
                // Check if loop contains transfer or call
                val sublines = lines.drop(index).take(15)
                val hasLoopTransfer = sublines.any { it.contains(".transfer(") || it.contains(".send(") || it.contains(".call{value:") }
                if (hasLoopTransfer) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-114",
                            title = "DoS with Failed Call (Push over Pull)",
                            severity = VulnerabilitySeverity.HIGH,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "Sending payments inside a loop to external addresses can permanently freeze the contract. If one recipient is a malicious contract that always reverts, the entire loop fails, blocking all other users.",
                            bengaliTip = "লুপের মধ্যে সবাইকে একসাথে টাকা পাঠাবেন না (Push payment)। কোনো একজন প্রাপক যদি রিসিভ ফাংশনে রিভার্ট করে রাখে, তবে পুরো লুপ বন্ধ হয়ে যাবে এবং বাকিরা কোনো টাকাই পাবে না। Pull Payment (উইথড্রয়াল) প্যাটার্ন দিন।",
                            exploitScenario = "Malicious contract joins dividend list and reverts in receive(). The distribute() transaction will always revert, permanently locking payouts.",
                            remediation = "Use the Pull over Push pattern: record user balances in a mapping and let each user withdraw their own funds individually.",
                            patchedSnippet = "// Pull pattern:\nbalances[payee] += amount;\n// User withdraws individually"
                        )
                    )
                }
            }
        }

        // 9. SWC-108: Missing State Variable Visibility
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("//") && !trimmed.contains("function ") && !trimmed.contains("event ") && !trimmed.contains("modifier ")) {
                val isStateVar = (trimmed.startsWith("uint") || trimmed.startsWith("int") || trimmed.startsWith("address") || trimmed.startsWith("bool") || trimmed.startsWith("bytes32")) &&
                        trimmed.endsWith(";") && !trimmed.contains("(")
                val hasVisibility = trimmed.contains("public") || trimmed.contains("private") || trimmed.contains("internal")
                if (isStateVar && !hasVisibility) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "SWC-108",
                            title = "State Variable Default Visibility",
                            severity = VulnerabilitySeverity.LOW,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "State variable does not declare an explicit visibility keyword. By default in Solidity, state variables default to internal, but omitting the keyword reduces readability and can lead to unintended assumptions.",
                            bengaliTip = "স্টেট ভ্যারিয়েবলে নির্দিষ্ট ভিজিবিলিটি (public, private বা internal) উল্লেখ করুন। কোনো কিছু না লিখলে কোড বোঝা কঠিন হয়।",
                            exploitScenario = "Developer assumes variable is private, but omitting visibility causes confusion or improper access in derived contracts.",
                            remediation = "Explicitly specify 'public', 'private', or 'internal'.",
                            patchedSnippet = trimmed.replace(";", " private;")
                        )
                    )
                }
            }
        }

        // 10. ERC-20 Unsafe Transfer
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("//") && (trimmed.contains(".transfer(") || trimmed.contains(".transferFrom(")) &&
                (trimmed.contains("token.") || trimmed.contains("IERC20") || trimmed.contains("ERC20")) &&
                !sourceCode.contains("SafeERC20")
            ) {
                findings.add(
                    VulnerabilityFinding(
                        id = UUID.randomUUID().toString(),
                        swcId = "SWC-104",
                        title = "Unsafe ERC20 Transfer (Missing SafeERC20)",
                        severity = VulnerabilitySeverity.MEDIUM,
                        lineNumber = index + 1,
                        codeSnippet = trimmed,
                        description = "Tokens such as USDT do not return a boolean on transfer(). Using standard IERC20.transfer() directly will revert when interacting with non-standard ERC-20 tokens, or fail to detect false return values.",
                        bengaliTip = "ERC-20 টোকেন লেনদেনে সরাসরি .transfer() ব্যবহার করবেন না (যেমন: USDT রিভার্ট ছাড়া ফলস দেয়)। OpenZeppelin-এর SafeERC20 (safeTransfer) লাইব্রেরি ব্যবহার করুন।",
                        exploitScenario = "Contract fails or locks funds when dealing with popular tokens like USDT that do not follow strict ERC-20 return specifications.",
                        remediation = "Import OpenZeppelin SafeERC20 and use 'using SafeERC20 for IERC20;' followed by 'token.safeTransfer(...)'.",
                        patchedSnippet = "using SafeERC20 for IERC20;\ntoken.safeTransfer(recipient, amount);"
                    )
                )
            }
        }

        // 11. Gas Optimization: String Require vs Custom Errors
        var longRequireCount = 0
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.contains("require(") && trimmed.contains("\"") && !trimmed.startsWith("//")) {
                longRequireCount++
                if (longRequireCount <= 2) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "GAS-OPT",
                            title = "Gas Optimization: Use Custom Errors Instead of Require Strings",
                            severity = VulnerabilitySeverity.GAS_OPTIMIZATION,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "Require strings consume considerable runtime gas for deployment and execution. Custom errors ('error Unauthorized();' with 'if (!cond) revert Unauthorized();') save up to 50 gas per check and reduce contract bytecode size.",
                            bengaliTip = "require(\"Error message\") ব্যবহারের চেয়ে Solidity 0.8.4+ এর Custom Error (যেমন: error NotOwner(); revert NotOwner();) ব্যবহার করলে গ্যাস খরচ অনেক কমে।",
                            exploitScenario = "Excessive gas consumption costs end users higher transaction fees and inflates contract size.",
                            remediation = "Define custom error: 'error Unauthorized();' and replace with: 'if (!condition) revert Unauthorized();'",
                            patchedSnippet = "error InvalidOperation();\nif (!condition) revert InvalidOperation();"
                        )
                    )
                }
            }
        }

        // 12. Gas Optimization: Public vs External
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("function ") && trimmed.contains(" public ") && !trimmed.contains("view") && !trimmed.contains("pure")) {
                val funcName = Regex("""function\s+([A-Za-z0-9_]+)""").find(trimmed)?.groupValues?.get(1)
                if (funcName != null && !sourceCode.contains("$funcName(") || sourceCode.indexOf("$funcName(") == sourceCode.lastIndexOf("$funcName(")) {
                    findings.add(
                        VulnerabilityFinding(
                            id = UUID.randomUUID().toString(),
                            swcId = "GAS-OPT",
                            title = "Gas Optimization: Function Can Be Restricted to 'external'",
                            severity = VulnerabilitySeverity.GAS_OPTIMIZATION,
                            lineNumber = index + 1,
                            codeSnippet = trimmed,
                            description = "Functions never called internally within the contract should be declared 'external' instead of 'public' to save gas, as external functions read parameters directly from calldata.",
                            bengaliTip = "যেসব ফাংশন শুধুমাত্র বাইরে থেকে কল করা হয় সেগুলোকে public এর বদলে external করুন। এতে প্যারামিটারগুলো calldata থেকে সরাসরি পড়া হয় এবং গ্যাস সাশ্রয় হয়।",
                            exploitScenario = "Unnecessary gas fees charged to users during contract interaction.",
                            remediation = "Change visibility from 'public' to 'external'.",
                            patchedSnippet = trimmed.replace(" public ", " external ")
                        )
                    )
                }
            }
        }

        // Calculate Security Health Score:
        // Base 100
        // Critical: -25 each
        // High: -15 each
        // Medium: -8 each
        // Low: -3 each
        // Gas: -1 each
        var penalty = 0
        findings.forEach {
            penalty += it.severity.weight
        }
        val calculatedScore = (100 - penalty).coerceIn(0, 100)

        return AuditReport(
            id = UUID.randomUUID().toString(),
            contractName = detectedName,
            timestamp = System.currentTimeMillis(),
            sourceCode = sourceCode,
            score = calculatedScore,
            sloc = sloc,
            functionCount = functionCount,
            stateVarCount = stateVarCount,
            compilerVersion = compilerVersion,
            findings = findings.sortedBy { it.severity.ordinal }
        )
    }
}

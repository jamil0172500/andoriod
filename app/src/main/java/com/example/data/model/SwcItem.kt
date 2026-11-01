package com.example.data.model

data class SwcItem(
    val swcId: String,
    val title: String,
    val severity: VulnerabilitySeverity,
    val summary: String,
    val bengaliExplanation: String,
    val vulnerablePattern: String,
    val securePattern: String
)

object SwcDatabase {
    val items = listOf(
        SwcItem(
            swcId = "SWC-107",
            title = "Reentrancy",
            severity = VulnerabilitySeverity.CRITICAL,
            summary = "Occurs when an external contract call takes control of execution flow and re-invokes the caller before the initial execution is finished.",
            bengaliExplanation = "TheDAO হ্যাকের অন্যতম মূল কারণ। স্টেট পরিবর্তনের আগে এক্সটার্নাল কল করার কারণে হ্যাকার বারবার উইথড্র ফাংশন ডেকে সব ব্যালেন্স খালি করে দেয়।",
            vulnerablePattern = "msg.sender.call{value: bal}(\"\");\nbalances[msg.sender] = 0;",
            securePattern = "balances[msg.sender] = 0;\n(bool s, ) = msg.sender.call{value: bal}(\"\");\nrequire(s, \"Failed\");"
        ),
        SwcItem(
            swcId = "SWC-115",
            title = "Authorization through tx.origin",
            severity = VulnerabilitySeverity.CRITICAL,
            summary = "tx.origin returns the original sender of the entire transaction chain. If an owner calls an attacker's contract, that contract can impersonate the owner.",
            bengaliExplanation = "tx.origin ফিশিংয়ের শিকার হতে পারে। ভিকটিম কোনো স্প্যাম ওয়েবসাইটে ট্রানজেকশন সাইন করলে হ্যাকার এই কন্ট্রাক্টের মালিক হিসেবে ফান্ডের দখল নিতে পারে।",
            vulnerablePattern = "require(tx.origin == owner);",
            securePattern = "require(msg.sender == owner);"
        ),
        SwcItem(
            swcId = "SWC-104",
            title = "Unchecked Call Return Value",
            severity = VulnerabilitySeverity.HIGH,
            summary = "Low-level functions (call, delegatecall, send) do not revert on failure; they return false. If not checked, execution continues blindly.",
            bengaliExplanation = "লো-লেভেল কল ফেইল করলেও এরর দেখায় না, শুধু false রিটার্ন দেয়। এটি চেক না করলে ট্রানজেকশন সফল হয়েছে ভেবে ভুল লজিক এক্সিকিউট হয়।",
            vulnerablePattern = "recipient.call{value: 1 ether}(\"\");",
            securePattern = "(bool ok, ) = recipient.call{value: 1 ether}(\"\");\nrequire(ok, \"Call reverted\");"
        ),
        SwcItem(
            swcId = "SWC-106",
            title = "Unprotected SELFDESTRUCT",
            severity = VulnerabilitySeverity.CRITICAL,
            summary = "Callable selfdestruct without proper access control allows malicious actors to erase contract code and siphon any stored native token balances.",
            bengaliExplanation = "সেলফডিস্ট্রাক্ট ফাংশনে এক্সেস রেস্ট্রিকশন না থাকলে যেকোনো কেউ কন্ট্রাক্ট সম্পূর্ণ মুছে দিতে পারে।",
            vulnerablePattern = "function destroy() public {\n    selfdestruct(payable(msg.sender));\n}",
            securePattern = "function destroy() external onlyOwner {\n    // Deprecated in EIP-6780, prefer pause flags.\n}"
        ),
        SwcItem(
            swcId = "SWC-112",
            title = "Delegatecall to Untrusted Callee",
            severity = VulnerabilitySeverity.CRITICAL,
            summary = "delegatecall executes external code with the storage and balance context of the caller. Untrusted targets can overwrite storage slot 0.",
            bengaliExplanation = "ডেলিগেটকল দিয়ে অন্যের কোড নিজের কন্ট্রাক্টের মেমরিতে রান হয়। টার্গেট সুরক্ষিত না হলে হ্যাকার মালিকানা বদলে নিতে পারে।",
            vulnerablePattern = "address(target).delegatecall(data);",
            securePattern = "require(target == approvedImplementation);\naddress(target).delegatecall(data);"
        ),
        SwcItem(
            swcId = "SWC-114",
            title = "DoS with Failed Call",
            severity = VulnerabilitySeverity.HIGH,
            summary = "Pushing payments inside loops can freeze the entire contract if one recipient deliberately reverts in receive() or runs out of gas.",
            bengaliExplanation = "লুপের মধ্যে সবাইকে একসাথে টাকা পাঠালে একজন প্রাপক রিভার্ট মারলেই পুরো প্রসেস লক হয়ে যাবে। এর বদলে উইথড্রয়াল প্যাটার্ন ব্যবহার করুন।",
            vulnerablePattern = "for(uint i=0; i<users.length; i++) {\n    users[i].transfer(rewards[i]);\n}",
            securePattern = "// Pull Pattern:\npayableBalances[user] += rewards;\n// user calls withdraw() individually"
        ),
        SwcItem(
            swcId = "SWC-116",
            title = "Block Values as Proxy for Time or Randomness",
            severity = VulnerabilitySeverity.MEDIUM,
            summary = "Miners can manipulate block.timestamp and block.prevrandao within boundaries, invalidating them as sources of true cryptographic randomness.",
            bengaliExplanation = "লটারি বা এলোমেলো সংখ্যার জন্য ব্লক টাইমস্ট্যাম্প বা ব্লকহ্যাশ ব্যবহার বিপজ্জনক, কারণ ভ্যালিডেটররা এগুলো নিজেদের পক্ষে ম্যানিপুলেট করতে পারে।",
            vulnerablePattern = "uint rand = uint(keccak256(abi.encodePacked(block.timestamp))) % 100;",
            securePattern = "// Use Chainlink VRF or Commit-Reveal scheme"
        ),
        SwcItem(
            swcId = "SWC-103",
            title = "Floating Pragma",
            severity = VulnerabilitySeverity.LOW,
            summary = "Contracts compiled with different compiler versions can introduce subtle bugs or unexpected compiler optimizer changes.",
            bengaliExplanation = "ফ্লোটিং প্র্যাগমা (^0.8.x) বিভিন্ন কম্পাইলারে বিভিন্ন ফলাফল দিতে পারে। নির্দিষ্ট ভার্সন লক করে দেওয়া নিরাপদ।",
            vulnerablePattern = "pragma solidity ^0.8.0;",
            securePattern = "pragma solidity 0.8.24;"
        )
    )
}

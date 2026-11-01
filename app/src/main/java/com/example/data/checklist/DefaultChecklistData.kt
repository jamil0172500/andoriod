package com.example.data.checklist

import com.example.data.model.ChecklistCategory
import com.example.data.model.ChecklistItem
import com.example.data.model.VulnerabilitySeverity

object DefaultChecklistData {
    fun getCategories(): List<ChecklistCategory> {
        return listOf(
            ChecklistCategory(
                name = "Reentrancy & Execution Flow",
                iconName = "Security",
                items = listOf(
                    ChecklistItem(
                        id = "chk_reentrancy_cei",
                        title = "Checks-Effects-Interactions (CEI) Pattern",
                        description = "State variables (such as user balances or status) must always be updated BEFORE making external calls (.call, .transfer, or external contract invocation).",
                        bengaliExplanation = "এক্সটার্নাল কলের পূর্বেই স্টেট বা ব্যালেন্স আপডেট করুন। এটি রিয়ান্ট্র্যান্সি প্রতিরোধ করার প্রথম নিয়ম।",
                        severity = VulnerabilitySeverity.CRITICAL,
                        swcReference = "SWC-107"
                    ),
                    ChecklistItem(
                        id = "chk_reentrancy_guard",
                        title = "ReentrancyGuard / nonReentrant Modifiers",
                        description = "Use OpenZeppelin's ReentrancyGuard mutex locks on all state-modifying functions that initiate external transfers.",
                        bengaliExplanation = "টাকা বা টোকেন ট্রান্সফার করে এমন সকল ফাংশনে nonReentrant মডিফায়ার নিশ্চিত করুন।",
                        severity = VulnerabilitySeverity.CRITICAL,
                        swcReference = "SWC-107"
                    ),
                    ChecklistItem(
                        id = "chk_pull_payments",
                        title = "Favor Pull over Push Payments",
                        description = "Never push payments to untrusted arrays in loops. Use pull payments where payees withdraw their owed amounts individually.",
                        bengaliExplanation = "একসাথে একাধিক অ্যাড্রেসে টাকা না পাঠিয়ে ব্যবহারকারীদের তাদের নিজস্ব ব্যালেন্স উইথড্র করার সুযোগ দিন।",
                        severity = VulnerabilitySeverity.HIGH,
                        swcReference = "SWC-114"
                    )
                )
            ),
            ChecklistCategory(
                name = "Access Control & Governance",
                iconName = "AdminPanelSettings",
                items = listOf(
                    ChecklistItem(
                        id = "chk_no_tx_origin",
                        title = "Never Use tx.origin for Authentication",
                        description = "Ensure msg.sender is used for access control instead of tx.origin, which is vulnerable to phishing relay attacks.",
                        bengaliExplanation = "অ্যাক্সেস কন্ট্রোলে কখনো tx.origin ব্যবহার করবেন না, সর্বদা msg.sender ব্যবহার করুন।",
                        severity = VulnerabilitySeverity.CRITICAL,
                        swcReference = "SWC-115"
                    ),
                    ChecklistItem(
                        id = "chk_owner_privileges",
                        title = "Privileged Functions Guarded & Timelocked",
                        description = "Sensitive actions like minting, pausing, upgrading, and parameter adjustments must be guarded by Ownable2Step or multi-sig timelocks.",
                        bengaliExplanation = "মিন্ট বা আপগ্রেড করার মতো সংবেদনশীল ফাংশনগুলো সুরক্ষিত এবং টাইম-লকযুক্ত রাখুন।",
                        severity = VulnerabilitySeverity.HIGH,
                        swcReference = "SWC-105"
                    ),
                    ChecklistItem(
                        id = "chk_no_selfdestruct",
                        title = "Avoid selfdestruct / suicide Calls",
                        description = "selfdestruct is deprecated in Cancun (EIP-6780). Ensure it is completely absent or restricted behind high-threshold multi-sig.",
                        bengaliExplanation = "Ethereum আপগ্রেডের পর selfdestruct পরিহার করুন। এর বদলে ইমার্জেন্সি পজ মেকানিজম ব্যবহার করুন।",
                        severity = VulnerabilitySeverity.HIGH,
                        swcReference = "SWC-106"
                    )
                )
            ),
            ChecklistCategory(
                name = "Oracles, MEV & Financial Math",
                iconName = "CurrencyBitcoin",
                items = listOf(
                    ChecklistItem(
                        id = "chk_no_spot_reserves",
                        title = "No Flash Loan Manipulable Spot Reserves",
                        description = "Do not rely on Uniswap/Pancake spot reserves (getReserves()) directly for price evaluation. Use Chainlink or Uniswap TWAP oracles.",
                        bengaliExplanation = "স্পট রিজার্ভের ওপর নির্ভর করে মূল্য হিসাব করবেন না, এতে ফ্ল্যাশ লোন দিয়ে দাম ম্যানিপুলেট করা যায়। TWAP বা Chainlink ব্যবহার করুন।",
                        severity = VulnerabilitySeverity.CRITICAL,
                        swcReference = "SWC-116"
                    ),
                    ChecklistItem(
                        id = "chk_slippage_protection",
                        title = "Slippage & Deadline Parameters on Swaps",
                        description = "Ensure minAmountOut and deadline timestamps exist on all token swap functions to protect users against MEV sandwich attacks.",
                        bengaliExplanation = "স্যান্ডউইচ ও MEV অ্যাটাক থেকে বাঁচাতে সোয়াপ ফাংশনে স্লিপেজ টলারেন্স এবং ডেডলাইন প্যারামিটার রাখুন।",
                        severity = VulnerabilitySeverity.HIGH,
                        swcReference = "SWC-114"
                    ),
                    ChecklistItem(
                        id = "chk_safe_erc20",
                        title = "SafeERC20 for Token Transfers",
                        description = "Tokens like USDT do not return boolean on transfer(). Always use OpenZeppelin SafeERC20 (safeTransfer, safeTransferFrom).",
                        bengaliExplanation = "নন-স্ট্যান্ডার্ড ERC20 টোকেন (যেমন USDT) হ্যান্ডেল করতে SafeERC20 ব্যবহার আবশ্যক।",
                        severity = VulnerabilitySeverity.MEDIUM,
                        swcReference = "SWC-104"
                    )
                )
            ),
            ChecklistCategory(
                name = "Compiler, Gas & Pre-Deployment",
                iconName = "Code",
                items = listOf(
                    ChecklistItem(
                        id = "chk_locked_pragma",
                        title = "Lock Stable Compiler Pragma",
                        description = "Avoid floating pragma (^0.8.x). Lock to exact audited compiler version (e.g. pragma solidity 0.8.24;).",
                        bengaliExplanation = "অডিটের সময় ব্যবহৃত নির্দিষ্ট স্টেবল কম্পাইলার ভার্সন লক করে রাখুন।",
                        severity = VulnerabilitySeverity.LOW,
                        swcReference = "SWC-103"
                    ),
                    ChecklistItem(
                        id = "chk_custom_errors",
                        title = "Custom Errors for Gas Efficiency",
                        description = "Replace string error messages in require() with Solidity 0.8.4+ custom errors (error CustomError();) to cut gas costs.",
                        bengaliExplanation = "require স্ট্রিং মেসেজের পরিবর্তে কাস্টম এরর ব্যবহার করে ডিপ্লয়মেন্ট এবং এক্সিকিউশন গ্যাস সাশ্রয় করুন।",
                        severity = VulnerabilitySeverity.GAS_OPTIMIZATION,
                        swcReference = "GAS-OPT"
                    ),
                    ChecklistItem(
                        id = "chk_events_emitted",
                        title = "Emit Events for Critical State Changes",
                        description = "All critical state updates (ownership change, transfers, fee updates, parameter changes) must emit indexed events for off-chain indexing.",
                        bengaliExplanation = "মালিকানা পরিবর্তন বা প্যারামিটার আপডেটের মতো গুরুত্বপূর্ণ কাজে ইভেন্ট এমিট নিশ্চিত করুন।",
                        severity = VulnerabilitySeverity.LOW,
                        swcReference = "SWC-108"
                    )
                )
            )
        )
    }
}

package com.example.data.sample

data class SampleContract(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val code: String
)

object SampleContracts {
    val samples = listOf(
        SampleContract(
            id = "reentrancy_vault",
            title = "TheDAO Style Reentrancy Vault",
            description = "Classic reentrancy flaw where external call precedes state updates, allowing recursive drain.",
            difficulty = "Critical Exploit",
            code = """
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract EtherVault {
    mapping(address => uint256) public balances;
    uint256 public totalVaultBalance;

    event Deposited(address indexed user, uint256 amount);
    event Withdrawn(address indexed user, uint256 amount);

    function deposit() external payable {
        require(msg.value > 0, "Deposit must be greater than zero");
        balances[msg.sender] += msg.value;
        totalVaultBalance += msg.value;
        emit Deposited(msg.sender, msg.value);
    }

    // VULNERABLE: State update happens AFTER external call!
    function withdrawAll() external {
        uint256 userBalance = balances[msg.sender];
        require(userBalance > 0, "No balance to withdraw");

        // External low-level call to msg.sender before zeroing out balance
        (bool success, ) = msg.sender.call{value: userBalance}("");
        require(success, "Ether transfer failed");

        // Checks-Effects-Interactions violation
        balances[msg.sender] = 0;
        totalVaultBalance -= userBalance;
        emit Withdrawn(msg.sender, userBalance);
    }
}
""".trimIndent()
        ),
        SampleContract(
            id = "tx_origin_wallet",
            title = "Phishable tx.origin MultiSig/Wallet",
            description = "Access control reliant on tx.origin instead of msg.sender, making it susceptible to phishing contracts.",
            difficulty = "Critical Exploit",
            code = """
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract PhishableWallet {
    address public owner;

    constructor() payable {
        owner = msg.sender;
    }

    receive() external payable {}

    // VULNERABLE: tx.origin can be spoofed via phishing proxy contract
    function transferTo(address payable recipient, uint256 amount) public {
        require(tx.origin == owner, "Caller is not the authorized owner");
        require(address(this).balance >= amount, "Insufficient contract balance");

        recipient.transfer(amount);
    }

    // VULNERABLE: Unprotected selfdestruct
    function emergencyKill() public {
        selfdestruct(payable(owner));
    }
}
""".trimIndent()
        ),
        SampleContract(
            id = "dos_push_payment",
            title = "Push Payment Denial-of-Service",
            description = "Distributing rewards in an unbounded loop with push transfers. One failing contract freezes everything.",
            difficulty = "High Risk",
            code = """
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

contract RewardDistributor {
    address[] public investors;
    mapping(address => uint256) public shares;

    function addInvestor(address investor, uint256 share) external {
        investors.push(investor);
        shares[investor] = share;
    }

    // VULNERABLE: DoS with Block Gas Limit and Malicious Reverting Recipient
    function distributeDividends() external payable {
        require(msg.value > 0, "Need dividend amount");
        for (uint256 i = 0; i < investors.length; i++) {
            address payable recipient = payable(investors[i]);
            uint256 payout = (msg.value * shares[recipient]) / 100;
            
            // Push payment in loop: if recipient is a contract that reverts, everything halts!
            recipient.transfer(payout);
        }
    }
}
""".trimIndent()
        ),
        SampleContract(
            id = "unchecked_return_bank",
            title = "Unchecked Return Value Bank",
            description = "Low-level .call without verifying boolean return status, leading to silent failures.",
            difficulty = "High Risk",
            code = """
// SPDX-License-Identifier: MIT
pragma solidity ^0.7.6; // Outdated compiler with overflow risk

contract SilentFailureBank {
    mapping(address => uint256) public userBalances;
    address owner; // Missing visibility

    function deposit() public payable {
        userBalances[msg.sender] += msg.value;
    }

    // VULNERABLE: Unchecked call return value
    function withdraw(uint256 amount) public {
        require(userBalances[msg.sender] >= amount, "Balance too low");
        userBalances[msg.sender] -= amount;

        // Return value is completely ignored!
        msg.sender.call{value: amount}("");
    }
}
""".trimIndent()
        ),
        SampleContract(
            id = "hardened_vault",
            title = "Hardened & Audited Production Vault",
            description = "Exemplary contract implementing ReentrancyGuard, Checks-Effects-Interactions, custom errors, and locked pragma.",
            difficulty = "Passed / Secure",
            code = """
// SPDX-License-Identifier: MIT
pragma solidity 0.8.24;

abstract contract ReentrancyGuard {
    uint256 private constant _NOT_ENTERED = 1;
    uint256 private constant _ENTERED = 2;
    uint256 private _status;

    error ReentrancyGuardReentrantCall();

    constructor() {
        _status = _NOT_ENTERED;
    }

    modifier nonReentrant() {
        if (_status == _ENTERED) revert ReentrancyGuardReentrantCall();
        _status = _ENTERED;
        _;
        _status = _NOT_ENTERED;
    }
}

contract SecureVault is ReentrancyGuard {
    error ZeroDeposit();
    error InsufficientBalance();
    error TransferFailed();
    error Unauthorized();

    address public immutable owner;
    mapping(address => uint256) private _balances;

    event Deposited(address indexed user, uint256 amount);
    event Withdrawn(address indexed user, uint256 amount);

    constructor() {
        owner = msg.sender;
    }

    function deposit() external payable {
        if (msg.value == 0) revert ZeroDeposit();
        _balances[msg.sender] += msg.value;
        emit Deposited(msg.sender, msg.value);
    }

    // SECURE: Checks-Effects-Interactions + nonReentrant modifier + custom errors
    function withdraw(uint256 amount) external nonReentrant {
        if (amount == 0 || _balances[msg.sender] < amount) revert InsufficientBalance();

        // 1. Effects: update state before interaction
        _balances[msg.sender] -= amount;

        // 2. Interactions: external call with checked return value
        (bool success, ) = msg.sender.call{value: amount}("");
        if (!success) revert TransferFailed();

        emit Withdrawn(msg.sender, amount);
    }

    function getBalance(address user) external view returns (uint256) {
        return _balances[user];
    }
}
""".trimIndent()
        )
    )
}

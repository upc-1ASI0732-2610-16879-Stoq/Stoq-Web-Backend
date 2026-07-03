package com.inventiapp.stocktrack.iam.domain.model.commands;

/**
 * Command for resetting a user's password without authentication.
 * @param email The account email
 * @param password The new password
 */
public record ResetPasswordCommand(String email, String password) {
}

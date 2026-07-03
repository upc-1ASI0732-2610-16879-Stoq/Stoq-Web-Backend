package com.inventiapp.stocktrack.iam.interfaces.rest.resources;

/**
 * Resource for password reset request.
 * @param email The account email
 * @param password The new password
 */
public record ResetPasswordResource(String email, String password) {
}

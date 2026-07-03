package com.inventiapp.stocktrack.iam.interfaces.rest.transform;

import com.inventiapp.stocktrack.iam.domain.model.commands.ResetPasswordCommand;
import com.inventiapp.stocktrack.iam.interfaces.rest.resources.ResetPasswordResource;

/**
 * Assembler to convert a ResetPasswordResource to a ResetPasswordCommand.
 */
public class ResetPasswordCommandFromResourceAssembler {
    /**
     * Converts a ResetPasswordResource to a ResetPasswordCommand.
     * @param resource The resource to convert
     * @return The reset password command
     */
    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.email(), resource.password());
    }
}

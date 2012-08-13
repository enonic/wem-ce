package com.enonic.wem.api.command.account;

import com.google.common.base.Preconditions;

import com.enonic.wem.api.account.AccountKey;
import com.enonic.wem.api.command.Command;

public final class ChangePassword
    extends Command<Boolean>
{
    private AccountKey key;

    private String password;

    public AccountKey getKey()
    {
        return this.key;
    }

    public String getPassword()
    {
        return this.password;
    }

    public ChangePassword key( final AccountKey key )
    {
        this.key = key;
        return this;
    }

    public ChangePassword password( final String password )
    {
        this.password = password;
        return this;
    }

    @Override
    public void validate()
    {
        Preconditions.checkNotNull( this.key, "Account key cannot be null" );
        Preconditions.checkNotNull( this.password, "Password cannot be null" );
        Preconditions.checkArgument( this.key.isUser(), "Cannot change password for non-user account" );
        Preconditions.checkArgument( !this.key.isAnonymous(), "Cannot change password for anonymous" );
        Preconditions.checkArgument( !this.key.isSuperUser(), "Cannot change password for super-user" );
    }
}

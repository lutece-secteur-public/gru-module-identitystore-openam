/*
 * Copyright (c) 2002-2016, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.modules.openam.services;

import fr.paris.lutece.plugins.identitystore.service.external.IIdentityInfoExternalProvider;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.openamidentityclient.business.Account;
import fr.paris.lutece.plugins.openamidentityclient.business.Identity;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityException;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * This class provides identity information from OpenAM
 */
public class IdentityInfoOpenAmProvider implements IIdentityInfoExternalProvider
{
    // Properties
    private static final String PROPERTIES_ATTRIBUTE_USER_NAME_GIVEN = "identitystore.openam.attribute.user.name.given";
    private static final String PROPERTIES_ATTRIBUTE_USER_NAME_FAMILLY = "identitystore.openam.attribute.user.name.family";
    private static final String PROPERTIES_ATTRIBUTE_USER_HOMEINFO_ONLINE_EMAIL = "identitystore.openam.attribute.user.home-info.online.email";
    private static final String ATTRIBUTE_IDENTITY_NAME_GIVEN = AppPropertiesService.getProperty( PROPERTIES_ATTRIBUTE_USER_NAME_GIVEN );
    private static final String ATTRIBUTE_IDENTITY_NAME_FAMILLY = AppPropertiesService.getProperty( PROPERTIES_ATTRIBUTE_USER_NAME_FAMILLY );
    private static final String ATTRIBUTE_IDENTITY_HOMEINFO_ONLINE_EMAIL = AppPropertiesService.getProperty( PROPERTIES_ATTRIBUTE_USER_HOMEINFO_ONLINE_EMAIL );

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityDto getIdentityInfo( String strGuid )
        throws IdentityNotFoundException
    {
        Identity identityOpenAM;
        Account accountOpenAM;
        IdentityDto identityDto = null;

        try
        {
            identityOpenAM = OpenamIdentityService.getService(  ).getIdentity( strGuid );
            accountOpenAM = OpenamIdentityService.getService(  ).getAccount( strGuid );

            if ( ( identityOpenAM != null ) && ( accountOpenAM != null ) )
            {
                identityDto = buildIdentity( identityOpenAM, accountOpenAM );
            }
        }
        catch ( OpenamIdentityException ex )
        {
            AppLogService.error( ex.getStackTrace(  ) );
            throw new IdentityNotFoundException( ex.getMessage(  ), ex );
        }

        return identityDto;
    }

    /**
     * Builds an identity from the specified OpenAM information
     * @param identityOpenAM identity of user
     * @param accountOpenAM Account of user
     * @return oIUserDTO populate of user
     */
    private static IdentityDto buildIdentity( Identity identityOpenAM, Account accountOpenAM )
    {
        IdentityDto identityDto = new IdentityDto(  );
        Map<String, AttributeDto> mapAttributes = new HashMap<String, AttributeDto>(  );

        identityDto.setConnectionId( identityOpenAM.getUid(  ) );
        identityDto.setAttributes( mapAttributes );

        setAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_GIVEN, identityOpenAM.getFirstname(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_NAME_FAMILLY, identityOpenAM.getLastname(  ) );
        setAttribute( identityDto, ATTRIBUTE_IDENTITY_HOMEINFO_ONLINE_EMAIL, accountOpenAM.getLogin(  ) );

        return identityDto;
    }

    /**
     * Sets an attribute into the specified identity
     * @param identityDto the identity
     * @param strCode the attribute code
     * @param strValue the attribute value
     */
    private static void setAttribute( IdentityDto identityDto, String strCode, String strValue )
    {
        AttributeDto attributeDto = new AttributeDto(  );
        attributeDto.setKey( strCode );
        attributeDto.setValue( strValue );

        identityDto.getAttributes(  ).put( attributeDto.getKey(  ), attributeDto );
    }
}

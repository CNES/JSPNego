/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This file is part of DOI-server.
 *
 * This JSPNego is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * JSPNego is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
/**
 * This package implements the protocol SPNego (Simple and Protected GSSAPI Negotiation Mechanism).
 * SPNEGO, often pronounced "spenay-go", is a {@link fr.cnes.jspnego.AbstractGSSClient GSSAPI} 
 * "pseudo mechanism" used by client-server software to negotiate the choice of security technology. 
 * SPNEGO is used when a client application wants to authenticate to a remote server, but neither 
 * end is sure what authentication protocols the other supports. The pseudo-mechanism uses a 
 * protocol to determine what common GSSAPI mechanisms are available, selects one and then 
 * dispatches all further security operations to it. This can help organizations deploy new security
 * mechanisms in a phased manner.
 * <p>
 * <img src="{@docRoot}/doc-files/gss.png" alt="GSS client">
 * </p> 
 */
package fr.cnes.jspnego;

package com.ooffice;

import junit.framework.TestCase;

public class ClienteOOfficeTest extends TestCase {
	
	public void testGenerarDocumento(){
		ClienteOOffice clienteOO = new ClienteOOffice();
		assertEquals(4, clienteOO.generarDocumento());
	}
}

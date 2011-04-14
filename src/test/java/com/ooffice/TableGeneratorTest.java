package com.ooffice;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class TableGeneratorTest extends TestCase {
	
	private static final String PIE = "Pie!!";

	private static final String CABECERA = "Cabecera!!!";

	public static final String RUTA_FICHERO_PLANTILLA = "c:\\ND_NOTIFICACION.sxw";
	
	public static final String RUTA_FICHERO_GENERADO = "c:\\GEN_ND_NOTIFICACION.sxw";
	
	public static final String INICIO_BOOKMARK = "inicio_decreto";
	
	public static final String FIN_BOOKMARK = "fin_decreto";
	
	public static final String TARGET_BOOKMARK = "inicio_decreto";
	
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public TableGeneratorTest(String testName) {
		super(testName);
	}
	
	public void setUp(){
		TableGenerator.initialize();
	}
	
	public void testBetweenBookmarks() {
		TableGenerator.betweenBookmarks(RUTA_FICHERO_PLANTILLA, RUTA_FICHERO_GENERADO, 
				INICIO_BOOKMARK, FIN_BOOKMARK, TARGET_BOOKMARK, CABECERA, PIE);
		assertTrue(true);
	}
}

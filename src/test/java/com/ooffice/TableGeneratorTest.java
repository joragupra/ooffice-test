package com.ooffice;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class TableGeneratorTest extends TestCase {
	
	private static final String PIE = "Pie!!";

	private static final String CABECERA = "Cabecera!!!";

	public static final String RUTA_FICHERO_PLANTILLA = "c:\\temp-test-ooffice\\ND_NOTIFICACION.sxw";
	
	public static final String RUTA_FICHERO_GENERADO = "c:\\temp-test-ooffice\\GEN_ND_NOTIFICACION.sxw";
	
	public static final String INICIO_BOOKMARK = "inicio_decreto";
	
	public static final String FIN_BOOKMARK = "fin_decreto";
	
	public static final String TARGET_BOOKMARK = "inicio_decreto";
	
	public static final int NUMERO_DOCUMENTOS_GENERADOS = 1000;
	
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
		assertEquals(1, generarDocumento());
	}
	
	private int generarDocumento(){
		return TableGenerator.betweenBookmarks(RUTA_FICHERO_PLANTILLA, crearFicheroDestino(), 
				INICIO_BOOKMARK, FIN_BOOKMARK, TARGET_BOOKMARK, CABECERA, PIE);
	}

	private String crearFicheroDestino() {
		File original = new File(RUTA_FICHERO_GENERADO);
		String[] ficheroExtension = RUTA_FICHERO_GENERADO.split("\\.");
		File copia = new File(ficheroExtension[0]+"_"+System.currentTimeMillis()+"."+ficheroExtension[1]);
		try {
			FileUtils.copyFile(original, copia);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copia.getAbsolutePath();
	}
}

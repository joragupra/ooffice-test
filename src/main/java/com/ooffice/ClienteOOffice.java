package com.ooffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.commons.io.FileUtils;



public class ClienteOOffice {
	
	/*                                                                    */
	/*                          CONSTANTES PUBLICAS                       */
	/* deberian estar configuradas en un fichero de propiedades o similar */
	/*                                                                    */
	public static final String SCRIPT = "/etc/init.d/OpenOfficeServidorMultiPuerto";
	
	public static final String ARRANQUE = "start";
	
	public static final String PARADA = "stop";
	
	public static final int TIEMPO_ESPERA = 10000;   //tiempo que hay que esperar dese que se arranca el proceso soffice.bin hasta que se puede empezar a usar
	
	public static final int PUERTO_INICIAL = 2003;  //a partir de este puerto se empiezan a buscar puertos libres para arrancar el proceso ooffice.bin
	
	public static final String SO_HOST = "LINUX"; //en el codigo se esperan los valores 'WINDOWS' o 'LINUX'
	/*                                                                    */
	/*                     FIN  CONSTANTES PUBLICAS                       */
	/*                                                                    */
	
	private static final int NUMERO_DOCUMENTOS_GENERADOS = 10;
	
	private static final String PIE = "Y esto va en el pie del nuevo documento.";

	private static final String CABECERA = "Esto va en la cabecera";

	private static final String RUTA_FICHERO_PLANTILLA = "/tmp/ooffice/ND_NOTIFICACION.sxw";

	private static final String RUTA_FICHERO_GENERADO = "/tmp/ooffice/GEN_ND_NOTIFICACION.sxw";

	private static final String INICIO_BOOKMARK = "inicio_decreto";

	private static final String FIN_BOOKMARK = "fin_decreto";

	private static final String TARGET_BOOKMARK = "inicio_decreto";
	
	/**
	 * <p>
	 * Genera tantos documentos como se definan en 
	 * <code>NUMERO_DOCUMENTOS_GENERADOS</code>. Se pueden lanzar varias 
	 * instancias de ejecuci&oacute;n para comprobar el comporamiento simulado 
	 * de varios usuarios generando documentos simult&aacute;neamente.
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ClienteOOffice prueba = new ClienteOOffice();
		for (int i = 0; i < NUMERO_DOCUMENTOS_GENERADOS; i++) {
			System.out.println("Generando documento " + (i+1));
			prueba.generarDocumento();
		}
	}
	
	/**
	 * <p>
	 * Muestra c&oacute;mo se deben generar los documentos en 3 pasos para que 
	 * no haya problemas con la memoria consumida por el proceso soffice.bin:
	 * </p>
	 * <p><ol>
	 * <li>Buscar un puerto a partir del inicial que est&eacute; libre para 
	 * poder arrancar el proceso soffice.bin</li>
	 * <li>Iniciar el proceso soffice.bin en el puerto indicado</li>
	 * <li>Conectar la aplicaci&oacute;n con el servidor OpenOffice arrancadoo 
	 * en el puerto que se encontr&oacute;</li>
	 * <li>Generar el documento</li>
	 * </ol></p>
	 * 
	 * @return
	 */
	public int generarDocumento() {
		System.out.println("Buscando puerto libre...");
		Socket socket = buscarPuertoLibre(PUERTO_INICIAL);
		System.out.println("Puerto encontrando: " + socket.getLocalPort());
		System.out.println("Iniciando proceso soffice...");
		ejecutarScriptProcesoOpenOffice(true, socket.getLocalPort());
		TableGenerator.initialize(socket.getLocalPort());
		int result = TableGenerator.betweenBookmarks(RUTA_FICHERO_PLANTILLA,
				crearFicheroDestino(), INICIO_BOOKMARK, FIN_BOOKMARK,
				TARGET_BOOKMARK, CABECERA, PIE);
		ejecutarScriptProcesoOpenOffice(false, socket.getLocalPort());
		System.out.println("Fin de la ejecucion de la prueba");
		return result;
	}
	
	public Socket buscarPuertoLibre(int initPort){
		Socket socket = null;
		int puerto = initPort;
		while(socket==null){
			try {
				socket = new Socket("localhost", puerto);
				socket.setReuseAddress(true);
			} catch (IOException e) {
				socket = null;
				puerto++;
			}
		}
		return socket;
	}
	
	/**
	 * <p>
	 * Lanza el script de arranque y parada de OpenOffice que se encuentra en 
	 * la ruta <code>SCRIPT</code>. El script se puede lanzar para arrancar el 
	 * proceso soffice (<code>isArranqueParada</code>=<i>true</i>) o para 
	 * deternerlo (<code>isArranqueParada</code>=<i>false</i>). El proceso 
	 * soffice escuchar&aacute; peticiones en el puerto indicado tras el 
	 * arranque (debe estar previamente escuchando en ese puerto en caso de 
	 * parada).
	 * </p>
	 * 
	 * @param isArranqueParada <i>true</i> para arrancar el proceso soffice y 
	 * <i>false</i> para pararlo.
	 * @param puerto Indica el puerto en el que se quiere que el proceso 
	 * soffice escuche peticiones.
	 * @return
	 */
	public Process ejecutarScriptProcesoOpenOffice(boolean isArranqueParada, int puerto){
		Process result = null;
		try {
			System.out.println("Lanzando script " + SCRIPT + " " + (isArranqueParada?ARRANQUE:PARADA));
			result = Runtime.getRuntime().exec(SCRIPT + " " + (isArranqueParada?ARRANQUE:PARADA) + " " + puerto);
			BufferedReader read=new BufferedReader(new InputStreamReader(result.getInputStream()));
			while(read.ready()) {
				System.out.println(read.readLine());
			}
			System.out.println("Script lanzado");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread t = Thread.currentThread();
		synchronized (t) {
			try {
				t.wait(TIEMPO_ESPERA);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("Despues de espera de " + TIEMPO_ESPERA + " ms");
		return result;
	}
	
	/**
	 * <p>
	 * Copia el fichero de <code>RUTA_FICHERO_GENERADO</code> a&ntilde;adiendo 
	 * un sufijo al mismo (sacado de <code>System.currentTimeMillis</code>) y 
	 * devuelve la ruta del nuevo fichero.
	 * </p>
	 * 
	 * @return
	 */
	private String crearFicheroDestino() {
		File original = new File(RUTA_FICHERO_GENERADO);
		String[] ficheroExtension = RUTA_FICHERO_GENERADO.split("\\.");
		File copia = new File(ficheroExtension[0] + "_"
				+ System.currentTimeMillis() + "." + ficheroExtension[1]);
		try {
			FileUtils.copyFile(original, copia);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copia.getAbsolutePath();
	}
}
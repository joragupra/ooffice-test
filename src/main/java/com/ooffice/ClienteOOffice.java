package com.ooffice;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.io.FileUtils;



public class ClienteOOffice {
	
	/*                                                                    */
	/*                          CONSTANTES PUBLICAS                       */
	/* deberian estar configuradas en un fichero de propiedades o similar */
	/*                                                                    */
	public static final String OOFFICE_PATH = "/opt/openoffice.org3/program/soffice.bin";
	
	private static final String SOCKET_OPTS = "-accept=socket,host=localhost,port=";
	
	public static final int TIEMPO_ESPERA = 120000;   //tiempo que hay que esperar dese que se arranca el proceso soffice.bin hasta que se puede empezar a usar
	
	public static final int PUERTO_INICIAL = 2002;  //a partir de este puerto se empiezan a buscar puertos libres para arrancar el proceso ooffice.bin
	
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
		System.out.println("Buscando puerto libre...");
		ServerSocket socket = buscarPuertoLibre(PUERTO_INICIAL);
		System.out.println("Puerto encontrando: " + socket.getLocalPort());
		System.out.println("Inicando proceso ooffice...");
		iniciarProcesoOpenOffice(socket.getLocalPort());
		System.out.println("Parando proceso soffice con script...");
		try {
			Runtime.getRuntime().exec(new String[]{"/etc/init.d/OpenOfficeServidor", "stop"});
			Thread t = Thread.currentThread();
			synchronized (t) {
				try {
					t.wait(TIEMPO_ESPERA);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static ServerSocket buscarPuertoLibre(int initPort){
		ServerSocket socket = null;
		int puerto = initPort;
		while(socket==null){
			try {
				socket = new ServerSocket(puerto);
				socket.setReuseAddress(true);
			} catch (IOException e) {
				socket = null;
				puerto++;
			}
		}
		return socket;
	}
	
	public static Process iniciarProcesoOpenOffice(int puerto){
		try {
			System.out.println("Arrancando servicio soffice con script...");
			String[] command = new String[]{"/etc/init.d/OpenOfficeServidor", "start"};
			for(String s : command){
				System.out.println(s);
			}
			Process result = Runtime.getRuntime().exec(command);
			Thread t = Thread.currentThread();
			synchronized (t) {
				try {
					t.wait(TIEMPO_ESPERA);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			System.out.println("Proceso arrancado despues de espera de " + TIEMPO_ESPERA + " ms");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

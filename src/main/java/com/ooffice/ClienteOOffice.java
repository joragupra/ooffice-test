package com.ooffice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;



public class ClienteOOffice {
	
	/*                                                                    */
	/*                          CONSTANTES PUBLICAS                       */
	/* deberian estar configuradas en un fichero de propiedades o similar */
	/*                                                                    */
	public static final String OOFFICE_PATH = "/opt/openoffice.org3/program/soffice.bin";
	
	private static final String SOCKET_OPTS = "-accept=socket,host=localhost,port=";
	
	public static final int TIEMPO_ESPERA = 60000;   //tiempo que hay que esperar dese que se arranca el proceso soffice.bin hasta que se puede empezar a usar
	
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
		Socket socket = buscarPuertoLibre(PUERTO_INICIAL);
		System.out.println("Puerto encontrando: " + socket.getLocalPort());
		System.out.println("Iniciando proceso ooffice...");
		try {
			Process result = Runtime.getRuntime().exec("/etc/init.d/OpenOfficeServidorArrancar start " + socket.getLocalPort());
			BufferedReader read=new BufferedReader(new InputStreamReader(result.getInputStream()));
			while(read.ready())
			{
				System.out.println(read.readLine());
			}
			System.out.println("Proceso arrancado");
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
		System.out.println("Transcurridos " + TIEMPO_ESPERA + " ms de espera");
		Process result2;
		try {
			result2 = Runtime.getRuntime().exec("/etc/init.d/OpenOfficeServidorParar stop " + socket.getLocalPort());
			BufferedReader read2=new BufferedReader(new InputStreamReader(result2.getInputStream()));
			while(read2.ready())
			{
			System.out.println(read2.readLine());
			}
			System.out.println("Proceso parado");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Fin de la ejecucion de la prueba");
	}
	
	public static Socket buscarPuertoLibre(int initPort){
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
}

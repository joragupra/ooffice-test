package com.ooffice;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;



public class ClienteOOffice {
	
	/*                                                                    */
	/*                          CONSTANTES PUBLICAS                       */
	/* deberian estar configuradas en un fichero de propiedades o similar */
	/*                                                                    */
//	public static final String OOFFICE_PATH = "/opt/openoffice.org2.4/program/soffice";
	
	public static final String OOFFICE_PATH = "C:\\Archivos de programa\\OpenOffice.org 2.4\\program\\soffice.exe";
	
	private static final String SOCKET_OPTS = "-accept=socket,host=localhost,port=";
	
	public static final int TIEMPO_ESPERA = 30000;   //tiempo que hay que esperar dese que se arranca el proceso soffice.bin hasta que se puede empezar a usar
	
	public static final int PUERTO_INICIAL = 2003;  //a partir de este puerto se empiezan a buscar puertos libres para arrancar el proceso ooffice.bin
	
	public static final String SO_HOST = "WINDOWS"; //en el codigo se esperan los valores 'WINDOWS' o 'LINUX'
	/*                                                                    */
	/*                     FIN  CONSTANTES PUBLICAS                       */
	/*                                                                    */
	
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
		System.out.println("Buscando puerto libre...");
		ServerSocket socket = prueba.buscarPuertoLibre(PUERTO_INICIAL);
		System.out.println("Puerto encontrando: " + socket.getLocalPort());
		System.out.println("Iniciando proceso soffice...");
		Process p = prueba.iniciarProcesoOpenOffice(socket.getLocalPort());
		System.out.println("Parando proceso soffice...");
		p.destroy();
		System.out.println("Liberando puerto " + socket.getLocalPort() + "...");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ServerSocket buscarPuertoLibre(int initPort){
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
	
	public Process iniciarProcesoOpenOffice(int puerto){
		try {
			System.out.println("Ejecutando commando " + OOFFICE_PATH + 
				    SOCKET_OPTS + puerto + ";urp;StarOffice.ServiceManager"
				    + " -headless" + " -nologo" + " -nofirststartwizard");
			ProcessBuilder pb = new ProcessBuilder(OOFFICE_PATH,
				    SOCKET_OPTS + puerto + ";urp;StarOffice.ServiceManager",
				    "-headless", "-nologo", "-nofirststartwizard");
			Map<String, String> env = pb.environment();
			if ("WINDOWS".equals(SO_HOST)) {
				System.out.println("Estableciendo entorno " + "c:\\user" + puerto);
			    env.put("USERPROFILE", "c:\\user"+puerto);
			} else {
				System.out.println("Estableciendo entorno " + "/tmp/user" + puerto);
			    env.put("HOME", "/tmp/user"+puerto);
			}
			System.out.println("Arrancando proceso...");
			Process result = pb.start();
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
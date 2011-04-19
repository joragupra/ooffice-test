package com.ooffice;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;



public class ClienteOOffice {
	
	/*                                                                    */
	/*                          CONSTANTES PUBLICAS                       */
	/* deberian estar configuradas en un fichero de propiedades o similar */
	/*                                                                    */
	public static final String TOMCAT_PATH = "/etc/init.d/TomcatService";
	
	public static final int TIEMPO_ESPERA = 120000;   //tiempo que hay que esperar dese que se arranca el proceso soffice.bin hasta que se puede empezar a usar
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
		System.out.println("Iniciando tomcat...");
		Process p = prueba.iniciarProcesoTomcat();
		System.out.println("Parando tomcat...");
		p.destroy();
	}
	
	public Process iniciarProcesoTomcat(){
		try {
			System.out.println("Ejecutando commando " + TOMCAT_PATH + 
				    " start");
			Process result = Runtime.getRuntime().exec(TOMCAT_PATH + " start");
			System.out.println("Espera...");
			Thread t = Thread.currentThread();
			synchronized (t) {
				try {
					t.wait(TIEMPO_ESPERA);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
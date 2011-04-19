package com.ooffice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.Vector;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.datatransfer.UnsupportedFlavorException;
import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.XTransferableSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.style.BreakType;
import com.sun.star.table.XCellRange;
import com.sun.star.text.ControlCharacter;
import com.sun.star.text.TableColumnSeparator;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.XBookmarksSupplier;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.text.XWordCursor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;


public class TableGenerator {

	
	static XComponentLoader xcomponentloader = null;

	static String stringConvertType = "writer_pdf_Export";

	static String stringExtension = "odt";

	static String indent = "";
	  
	//static XComponentContext xComponentContext = null;
	
	
	static String NOM_COL1 = "n� Registro";
	static String NOM_COL2 = "N� Fact.";
	static String NOM_COL3 = "Tercero";
	static String NOM_COL4 = "Descripci�n";
	static String NOM_COL5 = "Importe";
	static String NOM_COL6 = "Aplicaci�n";
	static String NOM_COL7 = "Operaci�n";
	static int 	  NUMBERS_COLS = 7;
	
	static String NOM_COL1_AEPSA = "N� FACT.";
	static String NOM_COL2_AEPSA = "FECHA";
	static String NOM_COL3_AEPSA = "EMPRESA";
	static String NOM_COL4_AEPSA = "IMPORTE";
	static int 	  NUMBERS_COLS_AEPSA = 4;
	
	
	public static void main(String[] args){
		initialize(2002);
		  
		Vector aux = new Vector();
		aux.add("C:\\TEMP\\3321209911268359.sxw");
		aux.add("C:\\TEMP\\3321209911294546.sxw");
		aux.add("C:\\TEMP\\3321209911294546.sxw");
		concatDocuments (aux, "C:\\TEMP\\3321209911318906.sxw");
		
	}
	  
	public synchronized static void initialize(int puerto) {
		      String ip_openoffice = "localhost";//ResourceBundle.getBundle("aplicacion").getString("ip.openoffice");
		      String puerto_openoffice = "" + puerto;//ResourceBundle.getBundle("aplicacion").getString("puerto.openoffice");
		      
		      System.out.println("Conectando a servicio con SocketOpenOfficeConnection en host="+ip_openoffice+",port="+puerto_openoffice+" ...");
		      OpenOfficeConnection connection = new SocketOpenOfficeConnection(ip_openoffice, puerto);
		      System.out.println("Conexion establecida correctamente");
		      
		      System.out.println("Obteniendo componentloader...");
		      xcomponentloader = connection.getDesktop();
		      System.out.println("componentloader obtenido correctamente");
		}
	
	/**
	���* 
	���* M�todo que copia el contenido del documento fuente comprendido entre los bookmarks init_bookmark_source y end_bookmark_source, en el 
	���* documento destino a partir del bookmark init_bokkmark_target y adem�s le a�ade texto al comienzo y al fin.
	���* 
	���* @param source URL del documento fuente
	���* @param target URL del documento destino
	���* @param init_bookmark_source Bookmark inicial del bloque a copiar (se coge la primera ocurrencia dentro del documento)
	���* @param end_bookmark_source Bookmark final del bloque a copiar
	���* @param init_bookmark_target Bookmark del documento destino a partir de la cual se copiar� la secci�n
	   * @param cabecera Texto de la cabecera
	   * @param pie Texto del pie
	���*/
	public synchronized static int betweenBookmarks(String source,
			String target, String init_bookmark_source,
			String end_bookmark_source, String init_bookmark_target,
			String cabecera, String pie) {

		int pages = 0;

		XComponent xComponent_sourceDoc = null;
		try {
			PropertyValue[] loadProps = new PropertyValue[1];
			loadProps[0] = new PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(true);
			xComponent_sourceDoc = TableGenerator.xcomponentloader
					.loadComponentFromURL(
							"file:///" + source.replace('\\', '/'), "_blank",
							0, loadProps);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		XComponent xComponent_targetDoc = null;
		try {
			PropertyValue[] loadProps = new PropertyValue[1];
			loadProps[0] = new PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(true);
			xComponent_targetDoc = TableGenerator.xcomponentloader
					.loadComponentFromURL(
							"file:///" + target.replace('\\', '/'), "_blank",
							0, loadProps);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		XTextDocument xTextDocument_sourceDoc = (XTextDocument) UnoRuntime
				.queryInterface(XTextDocument.class, xComponent_sourceDoc);
		XTextDocument xTextDocument_targetDoc = (XTextDocument) UnoRuntime
				.queryInterface(XTextDocument.class, xComponent_targetDoc);

		// the controllers

		XController xController_sourceDoc = xTextDocument_sourceDoc
				.getCurrentController();
		XController xController_targetDoc = xTextDocument_targetDoc
				.getCurrentController();

		XModel xModel_source = (XModel) UnoRuntime.queryInterface(XModel.class,
				xComponent_sourceDoc);
		XModel xModel_target = (XModel) UnoRuntime.queryInterface(XModel.class,
				xComponent_targetDoc);

		// Seleccionando la sección a copiar
		XBookmarksSupplier xBookmarksSupplier = (XBookmarksSupplier) UnoRuntime
				.queryInterface(XBookmarksSupplier.class, xComponent_sourceDoc);
		XNameAccess xNamedBookmarks = xBookmarksSupplier.getBookmarks();
		Object bookmark = null;
		Object bookmark1 = null;
		Object bookmark2 = null;

		try {

			bookmark1 = xNamedBookmarks.getByName(init_bookmark_source);
			bookmark2 = xNamedBookmarks.getByName(end_bookmark_source);
			bookmark = xNamedBookmarks.getByName(init_bookmark_source + "1");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			// e2.printStackTrace();
			bookmark = bookmark1;
		}

		XTextContent xBookmarkContent = (XTextContent) UnoRuntime
				.queryInterface(XTextContent.class, bookmark);
		XTextRange range1 = xBookmarkContent.getAnchor();
		xBookmarkContent = (XTextContent) UnoRuntime.queryInterface(
				XTextContent.class, bookmark2);
		XTextRange range2 = xBookmarkContent.getAnchor();

		XController xController_s = xModel_source.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier_sourceDoc = (XTextViewCursorSupplier) UnoRuntime
				.queryInterface(XTextViewCursorSupplier.class, xController_s);
		XTextViewCursor xTextViewCursor_sourceDoc = xViewCursorSupplier_sourceDoc
				.getViewCursor();
//		XText xDocumentText = xTextViewCursor_sourceDoc.getText();
//		XTextCursor xModelCursor = xDocumentText
//				.createTextCursorByRange(xTextViewCursor_sourceDoc.getStart());

		short s = 1;
		short s2 = 2;

		xTextViewCursor_sourceDoc.gotoRange(range1, false);
		xTextViewCursor_sourceDoc.goRight(s, false);

		try {
			xTextViewCursor_sourceDoc.gotoRange(range2, true);
		} catch (com.sun.star.uno.RuntimeException e2) {
			// TODO Auto-generated catch block
			// e2.printStackTrace();
			xTextViewCursor_sourceDoc.goLeft(s, false);
			xTextViewCursor_sourceDoc.gotoRange(range2, true);
		}

		xTextViewCursor_sourceDoc.goLeft(s2, true);

		// getting the data supplier of our source doc
		XTransferableSupplier xTransferableSupplier_sourceDoc = (XTransferableSupplier) UnoRuntime
				.queryInterface(XTransferableSupplier.class,
						xController_sourceDoc);
		// saving the selected contents
		XTransferable xTransferable = xTransferableSupplier_sourceDoc
				.getTransferable();

		// getting the data supplier of our target doc
		XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier) UnoRuntime
				.queryInterface(XTransferableSupplier.class,
						xController_targetDoc);

		// the cursor for the target document
		XController xController_t = xModel_target.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier_targetDoc = (XTextViewCursorSupplier) UnoRuntime
				.queryInterface(XTextViewCursorSupplier.class, xController_t);

		XTextViewCursor xTextViewCursor_targetDoc = xViewCursorSupplier_targetDoc
				.getViewCursor();
		XBookmarksSupplier xBookmarksSupplier_t = (XBookmarksSupplier) UnoRuntime
				.queryInterface(XBookmarksSupplier.class, xComponent_targetDoc);
		XNameAccess xNamedBookmarks_t = xBookmarksSupplier_t.getBookmarks();
		Object bookmark3 = null;

		try {
			bookmark3 = xNamedBookmarks_t.getByName(init_bookmark_target);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		XTextContent xBookmarkContent2 = (XTextContent) UnoRuntime
				.queryInterface(XTextContent.class, bookmark3);
		XTextRange range1_target = xBookmarkContent2.getAnchor();

		// XText xDocumentText_target = xTextViewCursor_targetDoc.getText();
		// XTextCursor xModelCursor_target =
		// xDocumentText_target.createTextCursorByRange(xTextViewCursor_targetDoc.getStart());
		xTextViewCursor_targetDoc.gotoRange(range1_target, false);

		try {
			if (!cabecera.equals("")) {

				xTextViewCursor_targetDoc.gotoEnd(false);
				xTextViewCursor_targetDoc.setString(cabecera);
				xTextViewCursor_targetDoc.gotoEnd(false);
			}

			// xTextViewCursor_targetDoc.gotoRange(range1_target.getEnd(),
			// false);
			xTransferableSupplier_targetDoc.insertTransferable(xTransferable);
			if (!pie.equals(""))
				xTextViewCursor_targetDoc.setString(pie);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		com.sun.star.frame.XStorable xStorable = (XStorable) UnoRuntime
				.queryInterface(XStorable.class, xTextDocument_targetDoc);
		try {
			boolean es = xStorable.isReadonly();
			xStorable.store();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		XController xController_t2 = xTextDocument_sourceDoc
				.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier_targetDoc2 = (XTextViewCursorSupplier) UnoRuntime
				.queryInterface(XTextViewCursorSupplier.class, xController_t2);

		XTextViewCursor xTextViewCursor_targetDoc2 = xViewCursorSupplier_targetDoc2
				.getViewCursor();
		xTextViewCursor_targetDoc2.getEnd();

		// Get a reference to the XPageCursor

		XPageCursor xPageCursor = (XPageCursor) UnoRuntime.queryInterface(
				XPageCursor.class, xTextViewCursor_targetDoc2);

		xPageCursor.jumpToLastPage();
		pages = xPageCursor.getPage();
		
		
		
		/* NUEVO */
//		xBookmarkContent.dispose();
//		xController_t.dispose();
//		xController_s.dispose();
//		xController_targetDoc.dispose();
//		xController_sourceDoc.dispose();
//		xController_t2.dispose();
		
		
		/* FIN NUEVO */
		
		
		xTextDocument_sourceDoc.dispose();
		xTextDocument_targetDoc.dispose();		

		return pages;
	}

	public synchronized static int betweenBookmarksLibro (String source, String target, String init_bookmark_source, String end_bookmark_source, String init_bookmark_target, String cabecera, String pie, String expediente) {
		
		int pages = 0;
		
		XComponent xComponent_sourceDoc = null;
		try {
			PropertyValue[] loadProps = new PropertyValue[1];
			loadProps[0] = new PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(true);
			xComponent_sourceDoc = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+source.replace('\\','/'), "_blank", 0, loadProps );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("//////EXPEDIENTE CORRUPTO: "+expediente);
			e.printStackTrace();
		}
		XComponent xComponent_targetDoc = null;
		try {
			PropertyValue[] loadProps = new PropertyValue[1];
			loadProps[0] = new PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(true);
			xComponent_targetDoc = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+target.replace('\\','/'), "_blank", 0, loadProps );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		XTextDocument xTextDocument_sourceDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,xComponent_sourceDoc);
		XTextDocument xTextDocument_targetDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent_targetDoc);
		
		//the controllers
		XController xController_sourceDoc = null;
		XController xController_targetDoc = null;
		XModel xModel_source = null;
		XModel xModel_target = null;
		
		boolean error = false;
		try{
			xController_sourceDoc = xTextDocument_sourceDoc.getCurrentController();
			xController_targetDoc = xTextDocument_targetDoc.getCurrentController();
			
			xModel_source = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_sourceDoc);
			xModel_target = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_targetDoc);
			
		}catch (NullPointerException e) {
			System.out.println("//////EXPEDIENTE CORRUPTO: "+expediente);
			error = true;
			e.printStackTrace();
		}
		
		if(!error){
			//Seleccionando la secci�n a copiar
			XBookmarksSupplier xBookmarksSupplier = (XBookmarksSupplier) UnoRuntime.queryInterface(XBookmarksSupplier.class, xComponent_sourceDoc);
			XNameAccess xNamedBookmarks = xBookmarksSupplier.getBookmarks();
			Object bookmark = null;
			Object bookmark1 = null;
			Object bookmark2 = null;
			
			
			try {
				
				bookmark1 = xNamedBookmarks.getByName(init_bookmark_source);
				bookmark2 = xNamedBookmarks.getByName(end_bookmark_source);
				bookmark = xNamedBookmarks.getByName(init_bookmark_source+"1");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				//e2.printStackTrace();
				bookmark = bookmark1;
			}
			
			XTextContent xBookmarkContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, bookmark1);
			XTextRange range1 = xBookmarkContent.getAnchor();
			xBookmarkContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, bookmark2);
			XTextRange range2 = xBookmarkContent.getAnchor();
			
			XController xController_s = xModel_source.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier_sourceDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_s);
			XTextViewCursor xTextViewCursor_sourceDoc = xViewCursorSupplier_sourceDoc.getViewCursor();
			XText xDocumentText = xTextViewCursor_sourceDoc.getText();
			XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xTextViewCursor_sourceDoc.getStart());
			
			short s = 1;
			short s2 = 2;
			
			xTextViewCursor_sourceDoc.gotoRange(range1, false);
			xTextViewCursor_sourceDoc.goRight(s, false);
			
			try {
				xTextViewCursor_sourceDoc.gotoRange(range2, true);
			} catch (com.sun.star.uno.RuntimeException e2) {
				// TODO Auto-generated catch block
				//e2.printStackTrace();
				xTextViewCursor_sourceDoc.goLeft(s, false);
				xTextViewCursor_sourceDoc.gotoRange(range2, true);
			}
			
			xTextViewCursor_sourceDoc.goLeft(s2, true);
			
			
			//getting the data supplier of our source doc
			XTransferableSupplier xTransferableSupplier_sourceDoc = (XTransferableSupplier) UnoRuntime.queryInterface(
					XTransferableSupplier.class, xController_sourceDoc);
			//saving the selected contents
			XTransferable xTransferable = xTransferableSupplier_sourceDoc.getTransferable();
			
			//getting the data supplier of our target doc
			XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier)UnoRuntime.queryInterface(
					XTransferableSupplier.class, xController_targetDoc);
			
			//the cursor for the target document
			XController xController_t = xModel_target.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier_targetDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t);
			
	//		XTextViewCursor xTextViewCursor_targetDoc = xViewCursorSupplier_targetDoc.getViewCursor();
			XBookmarksSupplier xBookmarksSupplier_t = (XBookmarksSupplier) UnoRuntime.queryInterface(XBookmarksSupplier.class, xComponent_targetDoc);
			XNameAccess xNamedBookmarks_t = xBookmarksSupplier_t.getBookmarks();
			Object bookmark3 = null;
			
			try {
				bookmark3 = xNamedBookmarks_t.getByName(init_bookmark_target);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			XTextContent xBookmarkContent2 = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, bookmark3);
			XTextRange range1_target = xBookmarkContent2.getAnchor();
			
			//XText xDocumentText_target = xTextViewCursor_targetDoc.getText();
			//XTextCursor xModelCursor_target = xDocumentText_target.createTextCursorByRange(xTextViewCursor_targetDoc.getStart());
	//		xTextViewCursor_targetDoc.gotoRange(range1_target, false);
			
			XTextDocument mxDoc = (XTextDocument)UnoRuntime.queryInterface( XTextDocument.class, xComponent_targetDoc);
			XText mxDocText = mxDoc.getText();
			
		
			try {
				
				if(!cabecera.equals("")){
				
					mxDocText.insertString ( range1_target, cabecera+"\n\n", false );
					
	//				xTextViewCursor_targetDoc.gotoEnd(false);
	//				xTextViewCursor_targetDoc.setString(cabecera);
	//				xTextViewCursor_targetDoc.gotoEnd(false);
					
	//				XWordCursor xWordCursor = null;
	//				xWordCursor = (XWordCursor) UnoRuntime.queryInterface(
	//						XWordCursor.class, range1_target);
	//
	//				// the PropertySet from the cursor contains the text attributes
	//				XPropertySet xPropertySet = (XPropertySet) UnoRuntime
	//						.queryInterface(XPropertySet.class, xWordCursor);
	//				
	//				xPropertySet.setPropertyValue("CharFontName", "Verdana");
	//				xPropertySet.setPropertyValue("CharWeight", new Float(com.sun.star.awt.FontWeight.NORMAL));
				}
				
				//xTextViewCursor_targetDoc.gotoRange(range1_target.getEnd(), false);
				xTransferableSupplier_targetDoc.insertTransferable(xTransferable);
				if(!pie.equals(""))
					mxDocText.insertString ( range1_target, pie+"\n\n", false );
	//				xTextViewCursor_targetDoc.setString(pie);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			com.sun.star.frame.XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xTextDocument_targetDoc);
			try {
				boolean es = xStorable.isReadonly();
				xStorable.store();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			XController xController_t2 = xTextDocument_sourceDoc.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier_targetDoc2 = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t2);
			
			XTextViewCursor xTextViewCursor_targetDoc2 = xViewCursorSupplier_targetDoc2.getViewCursor();
			xTextViewCursor_targetDoc2.getEnd();
			
			//Get a reference to the XPageCursor 
			
			XPageCursor xPageCursor = (XPageCursor)UnoRuntime.queryInterface(XPageCursor.class, xTextViewCursor_targetDoc2);
	
			xPageCursor.jumpToLastPage();
			pages = xPageCursor.getPage();
			
			xTextDocument_sourceDoc.dispose();
			xTextDocument_targetDoc.dispose();
		}else{
			xModel_target = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_targetDoc);
			//getting the data supplier of our target doc
			XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier)UnoRuntime.queryInterface(
					XTransferableSupplier.class, xController_targetDoc);
			
			//the cursor for the target document
			XController xController_t = xModel_target.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier_targetDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t);
			
//			XTextViewCursor xTextViewCursor_targetDoc = xViewCursorSupplier_targetDoc.getViewCursor();
			XBookmarksSupplier xBookmarksSupplier_t = (XBookmarksSupplier) UnoRuntime.queryInterface(XBookmarksSupplier.class, xComponent_targetDoc);
			XNameAccess xNamedBookmarks_t = xBookmarksSupplier_t.getBookmarks();
			Object bookmark3 = null;
			
			try {
				bookmark3 = xNamedBookmarks_t.getByName(init_bookmark_target);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			XTextContent xBookmarkContent2 = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, bookmark3);
			XTextRange range1_target = xBookmarkContent2.getAnchor();
			
			XTextDocument mxDoc = (XTextDocument)UnoRuntime.queryInterface( XTextDocument.class, xComponent_targetDoc);
			XText mxDocText = mxDoc.getText();
			
			mxDocText.insertString ( range1_target, "SE HA ENCONTRADO UN DOCUMENTO CORRUPTO DENTRO DE UN EXPEDIENTE. PONGASE" +
					" EN CONTACTO CON EL ADMINISTRADOR PARA SU CORRECCION.\n" +
					"LA REFERENCIA DEL EXPEDIENTE ES: "+expediente+"\n", false );
			
			
			com.sun.star.frame.XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xTextDocument_targetDoc);
			try {
				boolean es = xStorable.isReadonly();
				xStorable.store();
				xTextDocument_targetDoc.dispose();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		return pages;
	}
	
	public synchronized static int cuentaPaginas (String source) {
		
		int pages = 0;
		
		XComponent xComponent_sourceDoc = null;
		try {
			PropertyValue[] loadProps = new PropertyValue[1];
			loadProps[0] = new PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(true);
			xComponent_sourceDoc = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+source.replace('\\','/'), "_blank", 0, loadProps );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
				
		XTextDocument xTextDocument_sourceDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,xComponent_sourceDoc);
		
		
		XController xController_t2 = xTextDocument_sourceDoc.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier_sourceDoc2 = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t2);
		
		XTextViewCursor xTextViewCursor_sourceDoc2 = xViewCursorSupplier_sourceDoc2.getViewCursor();
		xTextViewCursor_sourceDoc2.getEnd();
		
		//Get a reference to the XPageCursor 
		
		XPageCursor xPageCursor = (XPageCursor)UnoRuntime.queryInterface(XPageCursor.class, xTextViewCursor_sourceDoc2);

		xPageCursor.jumpToLastPage();
		pages = xPageCursor.getPage();
		
		xTextDocument_sourceDoc.dispose();
		
		return pages;
	}
	
	 /** This method sets the text colour of the cell refered to by sCellName to
    white and inserts the string sText in it
 */
	protected synchronized static void insertIntoCell(String sCellName, String sText,
	                                     XTextTable xTable, int sizeChar, int colorChar, String alineacion) 
	{
	    // Access the XText interface of the cell referred to by sCellName	
	    XText xCellText = (XText) UnoRuntime.queryInterface(
	        XText.class, xTable.getCellByName ( sCellName ) );
	    
	    //Alinear centrado el texto
	    XTextCursor textCursor = xCellText.createTextCursor(); 
	    XParagraphCursor xPC = ( XParagraphCursor ) UnoRuntime.queryInterface(XParagraphCursor.class, textCursor );
	    XPropertySet xPS = ( XPropertySet ) UnoRuntime.queryInterface(XPropertySet.class, xPC );
	    try {
	    	if(alineacion.equals("BLOCK"))
	    		xPS.setPropertyValue( "ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT );
	    	else
	    		xPS.setPropertyValue( "ParaAdjust", com.sun.star.style.ParagraphAdjust.CENTER );
		} catch (UnknownPropertyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WrappedTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    // create a text cursor from the cells XText interface
	    XTextCursor xCellCursor = xCellText.createTextCursor();
	    // Get the property set of the cell's TextCursor
	    XPropertySet xCellCursorProps = (XPropertySet)UnoRuntime.queryInterface(
	        XPropertySet.class, xCellCursor );
	    
	    
	    try 
	    {
	        // Set the colour of the text to white
	        //xCellCursorProps.setPropertyValue( "CharColor", new Integer(16777215));
	    	xCellCursorProps.setPropertyValue("CharFontName", "Verdana");
	    	xCellCursorProps.setPropertyValue("CharWeight", new Float(com.sun.star.awt.FontWeight.NORMAL));
	    	xCellCursorProps.setPropertyValue( "CharColor", new Integer(colorChar));
	    	xCellCursorProps.setPropertyValue( "CharHeight", new Integer(sizeChar));
	    } 
	    catch ( Exception e) 
	    {
	        e.printStackTrace();
	    }
	    // Set the text in the cell to sText
	    xCellText.setString( sText );
	}
	
	/**
	 * Metodo que concatena N documentos OpenOffice en un solo documento. 
	 * 
	 * @param source Vector de documentos de entrada. Cada documento viene representado por su ruta absoluta en disco 
	 * @param target Ruta en disco del documento de salida
	 */
	public synchronized static void concatDocuments (Vector source, String target) {
		
		if (source.size() > 0)
			target = (String) source.elementAt(0);
		XComponent  xComponent_targetDoc = null;;
	    try {
			PropertyValue[] loadProps = new PropertyValue[1];
	    	loadProps[0] = new PropertyValue();
	    	loadProps[0].Name = "Hidden";
	    	loadProps[0].Value = new Boolean(true);
	    	xComponent_targetDoc  = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+target.replace('\\','/'), "_blank", 0, loadProps );
	    	//xComponent_targetDoc  = ConcatDocuments.xcomponentloader.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		XTextDocument xTextDocument_targetDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent_targetDoc);
		XController xController_targetDoc =  xTextDocument_targetDoc.getCurrentController();
		XModel xModel_target = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_targetDoc);
		XController xController_t = xModel_target.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier_targetDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t);
    	XTextViewCursor xTextViewCursor_targetDoc = xViewCursorSupplier_targetDoc.getViewCursor();
    	xTextViewCursor_targetDoc.gotoEnd(false);
		// Fin abrir documento destino
		
		//Recorremos los documentos fuente
		for (int i=1; i<source.size(); i++) {
			XComponent  xComponent_sourceDoc = null;;
			String file = (String) source.elementAt(i);
		    try {
				PropertyValue[] loadProps = new PropertyValue[1];
		    	loadProps[0] = new PropertyValue();
		    	loadProps[0].Name = "Hidden";
		    	loadProps[0].Value = new Boolean(true);
		    	xComponent_sourceDoc  = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+file.replace('\\','/'), "_blank", 0, loadProps );
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				continue;
			}
			XTextDocument xTextDocument_sourceDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent_sourceDoc);
			XController xController_sourceDoc =  xTextDocument_sourceDoc.getCurrentController();
			XModel xModel_source = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_sourceDoc);
			XController xController_s = xModel_source.getCurrentController();
			
		    XTextViewCursorSupplier xViewCursorSupplier_sourceDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_s);
            XTextViewCursor xTextViewCursor_sourceDoc = xViewCursorSupplier_sourceDoc.getViewCursor();		    
		    //XText xDocumentText = xTextViewCursor_sourceDoc.getText();
		    //XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xTextViewCursor_sourceDoc.getStart());
		    xTextViewCursor_sourceDoc.gotoStart(false);
		    xTextViewCursor_sourceDoc.gotoEnd(true);
		    
        	//getting the data supplier of our source doc
        	XTransferableSupplier xTransferableSupplier_sourceDoc = (XTransferableSupplier) UnoRuntime.queryInterface(XTransferableSupplier.class, xController_sourceDoc);
             //saving the selected contents
        	XTransferable xTransferable = xTransferableSupplier_sourceDoc.getTransferable();

            //getting the data supplier of our target doc
       	    XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier)UnoRuntime.queryInterface(
       	                                 XTransferableSupplier.class, xController_targetDoc);
       	   
       	    
       	    try {
       	    	try {
       	    		xTextViewCursor_targetDoc.getText().insertControlCharacter( xTextViewCursor_targetDoc.getText().getEnd(), ControlCharacter.PARAGRAPH_BREAK, false ); 
					XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xTextViewCursor_targetDoc.getText().getEnd());
					propertySet.setPropertyValue("BreakType", BreakType.PAGE_AFTER); 
					
					//xTextViewCursor_targetDoc.getText().insertControlCharacter(xTextViewCursor_targetDoc.getText().getEnd(), ControlCharacter, false);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnknownPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				xTransferableSupplier_targetDoc.insertTransferable(xTransferable);
				
				
				
				
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xTextDocument_sourceDoc.dispose();
			
		}		
		
		//Guardar documento destino
		com.sun.star.frame.XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xTextDocument_targetDoc);
        try {
			//xStorable.store();
        	PropertyValue[] storeProps = new PropertyValue[0];
            xStorable.storeAsURL("file:///" + target.replace('\\','/'), storeProps); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		xTextDocument_targetDoc.dispose();
		
	}
	
	/**
	 * Metodo que concatena N documentos OpenOffice en un solo documento. 
	 * 
	 * @param source Vector de documentos de entrada. Cada documento viene representado por su ruta absoluta en disco 
	 * @param target Ruta en disco del documento de salida
	 */
	public synchronized static void concatDocumentsAlReves (Vector source, String target) {
		if (source.size() > 0)
			target = (String) source.elementAt(source.size()-1);
		
		//Recorremos los documentos fuente
		for (int i=source.size()-2; i>=0; i--) {
		XComponent  xComponent_targetDoc = null;;
		    try {
				PropertyValue[] loadProps = new PropertyValue[1];
		    	loadProps[0] = new PropertyValue();
		    	loadProps[0].Name = "Hidden";
		    	loadProps[0].Value = new Boolean(true);
		    	xComponent_targetDoc  = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+target.replace('\\','/'), "_blank", 0, loadProps );
		    	//xComponent_targetDoc  = ConcatDocuments.xcomponentloader.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps );
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			XTextDocument xTextDocument_targetDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent_targetDoc);
			XController xController_targetDoc =  xTextDocument_targetDoc.getCurrentController();
			XModel xModel_target = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_targetDoc);
			XController xController_t = xModel_target.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier_targetDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_t);
	    	XTextViewCursor xTextViewCursor_targetDoc = xViewCursorSupplier_targetDoc.getViewCursor();
	    	xTextViewCursor_targetDoc.gotoEnd(false);
			// Fin abrir documento destino
			
//		//Recorremos los documentos fuente
//		for (int i=source.size()-2; i>=0; i--) {
			XComponent  xComponent_sourceDoc = null;;
			String file = (String) source.elementAt(i);
		    try {
				PropertyValue[] loadProps = new PropertyValue[1];
		    	loadProps[0] = new PropertyValue();
		    	loadProps[0].Name = "Hidden";
		    	loadProps[0].Value = new Boolean(true);
		    	xComponent_sourceDoc  = TableGenerator.xcomponentloader.loadComponentFromURL("file:///"+file.replace('\\','/'), "_blank", 0, loadProps );
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				continue;
			}
			XTextDocument xTextDocument_sourceDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent_sourceDoc);
			XController xController_sourceDoc =  xTextDocument_sourceDoc.getCurrentController();
			XModel xModel_source = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent_sourceDoc);
			XController xController_s = xModel_source.getCurrentController();
			
		    XTextViewCursorSupplier xViewCursorSupplier_sourceDoc = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController_s);
            XTextViewCursor xTextViewCursor_sourceDoc = xViewCursorSupplier_sourceDoc.getViewCursor();		    
		    //XText xDocumentText = xTextViewCursor_sourceDoc.getText();
		    //XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xTextViewCursor_sourceDoc.getStart());
		    xTextViewCursor_sourceDoc.gotoStart(false);
		    xTextViewCursor_sourceDoc.gotoEnd(true);
		    
        	//getting the data supplier of our source doc
        	XTransferableSupplier xTransferableSupplier_sourceDoc = (XTransferableSupplier) UnoRuntime.queryInterface(XTransferableSupplier.class, xController_sourceDoc);
             //saving the selected contents
        	XTransferable xTransferable = xTransferableSupplier_sourceDoc.getTransferable();

            //getting the data supplier of our target doc
       	    XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier)UnoRuntime.queryInterface(
       	                                 XTransferableSupplier.class, xController_targetDoc);
       	   
       	    
       	    try {
       	    	try {
       	    		xTextViewCursor_targetDoc.getText().insertControlCharacter( xTextViewCursor_targetDoc.getText().getEnd(), ControlCharacter.PARAGRAPH_BREAK, false ); 
					XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xTextViewCursor_targetDoc.getText().getEnd());
					propertySet.setPropertyValue("BreakType", BreakType.PAGE_AFTER); 
					
					//xTextViewCursor_targetDoc.getText().insertControlCharacter(xTextViewCursor_targetDoc.getText().getEnd(), ControlCharacter, false);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnknownPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				xTransferableSupplier_targetDoc.insertTransferable(xTransferable);
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xTextDocument_sourceDoc.dispose();
			
			//Guardar documento destino. Lo iremos guardando cada vez.
			com.sun.star.frame.XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xTextDocument_targetDoc);
	        try {
				//xStorable.store();
	        	PropertyValue[] storeProps = new PropertyValue[0];
	            xStorable.storeAsURL("file:///" + target.replace('\\','/'), storeProps); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			xTextDocument_targetDoc.dispose();
		}
	}
	
	public synchronized static String redondea(float resultado){
		BigDecimal bd = new BigDecimal(resultado);
		bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
		resultado = bd.floatValue();
		String res = String.valueOf(resultado);
		
		return res;
	}
}

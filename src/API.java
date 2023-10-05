import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.Scanner;

public class API {

     /*PLANTEAMIENTO
    línea de comandos
    "C:\Users\Rafael Arce Gaxiola\Desktop\DSIII\API XML DOM\src"
    cd "C:\Users\Rafael Arce Gaxiola\Desktop\DSIII\API XML DOM\out\production\API XML DOM"

    EJECUTAR EN LÍNEA DE COMANDOS
    "C:\Program Files\Java\jdk-11.0.16\bin\java.exe" -Dfile.encoding=UTF-8 -classpath "C:\Users\Rafael Arce Gaxiola\Desktop\DSIII\API XML DOM\out\production\API XML DOM" API "C:\Users\Rafael Arce Gaxiola\Desktop\DSIII\API XML DOM\src\sales.xml"
    * */


    static final String CLASS_NAME = API.class.getSimpleName();
    static final Logger LOG = Logger.getLogger(CLASS_NAME);

    public static void main(String argv[]) {
        if (argv.length != 1) {
            LOG.severe("Falta archivo XML como argumento.");
            System.exit(1);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(argv[0]));
            doc.getDocumentElement().normalize();

            double porcentajeIncremento = solicitarPorcentajeIncremento();
            String nombreDepartamento = solicitarNombreDepartamento();
            Element root = doc.getDocumentElement();

            // Usar un HashMap para rastrear las ventas de cada departamento
            HashMap<String, Double> ventasPorDepartamento = new HashMap<>();

            NodeList saleRecords = root.getElementsByTagName("sale_record");

            for (int i = 0; i < saleRecords.getLength(); i++) {
                Element saleRecord = (Element) saleRecords.item(i);
                Element departmentElement = (Element) saleRecord.getElementsByTagName("department").item(0);
                String departmentName = departmentElement.getTextContent();
                double ventas = Double.parseDouble(saleRecord.getElementsByTagName("sales").item(0).getTextContent());

                if (ventasPorDepartamento.containsKey(departmentName)) {
                    double totalVentas = ventasPorDepartamento.get(departmentName);
                    ventasPorDepartamento.put(departmentName, totalVentas + ventas);
                } else {
                    ventasPorDepartamento.put(departmentName, ventas);
                }
            }

            //TUVE VARIOS ERRORES, ASÍ QUE AGREGUÉ ESTOS SOUTS PARA DEPURAR Y ENCONTRAR LOS ERRORES QUE TENÍA
            System.out.println("DEPARTAMENTOS DEL XML");
            for (String departamento : ventasPorDepartamento.keySet()) {
                System.out.println(departamento);
            }

            //RESULTADO A TODOS LOS DEPAS IGUALES
            aplicarIncrementoATodos(root, nombreDepartamento, porcentajeIncremento);
            guardarNuevoDocumento(doc, "nuevas_ventas.xml");
            System.out.println("Se creó el archivo: nuevas_ventas.xml");

            //INFORME
            reporteVentas(ventasPorDepartamento);

        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }

    private static double solicitarPorcentajeIncremento() {
        double porcentaje = 0.0;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                //APLICAMOS EL PORCENTAJE A ELEGIR
                System.out.print("DAME EL PORCENTAJE A INGRESAR, AMIGO (entre 5% y 15%): ");
                porcentaje = Double.parseDouble(scanner.nextLine());
                if (porcentaje >= 5.0 && porcentaje <= 15.0) {
                    break;
                } else {
                    System.out.println("El porcentaje debe estar entre 5% y 15%.");
                }
            } catch (NumberFormatException e) {//ERROR EN EL FORMATO
                System.out.println("ERROR EN EL PORCENTAJE DADO");
            }
        }
        return porcentaje;
    }

    private static String solicitarNombreDepartamento() { //METODO 2
        Scanner scanner = new Scanner(System.in);
        System.out.print("DAME EL NOMBRE DEL DEPARTAMENTO: ");
        return scanner.nextLine();
    }

    private static void aplicarIncrementoATodos(Element root, String nombreDepartamento, double porcentajeIncremento) {
        NodeList saleRecords = root.getElementsByTagName("sale_record");

        //INCREMENTAR TODOS LOS DEPARTAMENTOS CON EL MISMO NOMBRE

        for (int i = 0; i < saleRecords.getLength(); i++) {
            Element saleRecord = (Element) saleRecords.item(i);
            Element departmentElement = (Element) saleRecord.getElementsByTagName("department").item(0);
            String departmentName = departmentElement.getTextContent();

            // Verificamos si el nombre del departamento coincide
            if (departmentName.equals(nombreDepartamento)) {
                aplicarIncremento(saleRecord, porcentajeIncremento); // Aplicamos el incremento al departamento
            }
        }
    }

    //PODERES
    private static void aplicarIncremento(Element saleRecord, double porcentajeIncremento) {
        Element salesElement = (Element) saleRecord.getElementsByTagName("sales").item(0);
        double ventasActuales = Double.parseDouble(salesElement.getTextContent());
        double nuevasVentas = ventasActuales * (1 + (porcentajeIncremento / 100));
        salesElement.setTextContent(String.format("%.2f", nuevasVentas));
    }

    private static void guardarNuevoDocumento(Document doc, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            OutputStream outputStream = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);
            outputStream.close();
        } catch (Exception e) {
            LOG.severe("ERROR ARCHIVO GUARDADO ERRONEAMENTE, VOLVER A INTENTAR " + e.getMessage());
        }
    }

    private static void reporteVentas(HashMap<String, Double> ventasPorDepartamento) {
        double totalVentas = 0.0;

        for (Double ventas : ventasPorDepartamento.values()) {
            totalVentas += ventas;
        }

        System.out.println("INFORMES: ");
        for (Map.Entry<String, Double> entry : ventasPorDepartamento.entrySet()) {
            String departamento = entry.getKey();
            double ventas = entry.getValue();
            double porcentaje = (ventas / totalVentas) * 100;
            System.out.printf("%-15.15s %,7.2f  (%2.2f %%)\n", departamento, ventas, porcentaje);
        }
    }
}

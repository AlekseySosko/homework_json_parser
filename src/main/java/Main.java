import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, ParseException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "new_data.json");

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "new_data2.json");

        String json3 = readString("new_data.json");
        List<Employee> list3 = jsonToList(json3);
        for (Employee employee : list3) {
            System.out.println(employee);
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> result = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csvToBean = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            result = csvToBean.parse();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        Type listType = new TypeToken<List<Employee>>() {
        }.getType();

        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));

        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        List<Employee> list = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element element = (Element) node;
                long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                String country = element.getElementsByTagName("country").item(0).getTextContent();
                int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());

                list.add(new Employee(id, firstName, lastName, country, age));
            }
        }

        return list;
    }

    public static String readString(String fileName) {
        StringBuilder str = new StringBuilder("");
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String s = "";
            while ((s = reader.readLine()) != null) {
                str.append(s);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return str.toString();
    }

    public static List<Employee> jsonToList(String json) throws ParseException {
        List<Employee> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(json);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        for (Object jsonObject : array) {
            Employee employee = gson.fromJson(jsonObject.toString(), Employee.class);
            list.add(employee);
        }
        return list;
    }
}

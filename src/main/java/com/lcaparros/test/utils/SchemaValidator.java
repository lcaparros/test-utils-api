package com.lcaparros.test.utils;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author lcaparros
 */

@Slf4j
public class SchemaValidator {
    private XPathExpression xpathExpression;
    private Validator validator;

    @Inject
    public SchemaValidator(){
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.xsd")){
            init(inputStream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SchemaValidator(InputStream inputStream) {
        init(inputStream);
    }

    private void init(InputStream inputStream) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Source xsdFile = new StreamSource(inputStream);

            Schema schema = schemaFactory.newSchema(xsdFile);
            validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            xpathExpression = XPathFactory.newInstance().newXPath().compile("//*[local-name()='Envelope']/*[local-name()='Body']/*");
        } catch (SAXException | XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public boolean validate(Document doc) throws TestException {
        try {
            Node node = (Node)xpathExpression.evaluate(doc, XPathConstants.NODE);

            Source source = new DOMSource(node);

            validator.validate(source);

            return true;
        } catch (XPathExpressionException | IOException e) {
            throw new TestException(e.getMessage());
        } catch (SAXException e) {
            return false;
        }
    }

    public static boolean soapFaultIsValid(String soapRequest) throws TestException {
        return soapRequestIsValid(soapRequest, "soap_schema.xsd");
    }

    public static boolean soapRequestIsValid(String soapRequest, String schemaPath) throws TestException {
        try {
            InputStream schema = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
            SchemaValidator schemaValidator = new SchemaValidator(schema);
            Document doc = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);

            doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(soapRequest.getBytes()));
            return schemaValidator.validate(doc);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TestException(e.getMessage());
        }
    }
}
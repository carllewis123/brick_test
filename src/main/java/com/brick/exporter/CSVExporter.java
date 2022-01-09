package com.brick.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.brick.model.Product;

public class CSVExporter {
    private static final String DELIMITTER = ";";
    private static final String FILE_NAME = "phones.csv";
    private static Path path = Paths.get(FILE_NAME);
    
    public static void export(List<Product> products) {
        try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))){
            writer.write(
                    "Name" + DELIMITTER
                    + "Description" + DELIMITTER
                    + "Img Link" + DELIMITTER
                    + "Price" + DELIMITTER
                    + "Rating" + DELIMITTER
                    + "Seller"
            );
            if(products == null)
                return;
            for (Product p : products) {
                writer.write(
                    "\n" + p.getName() + DELIMITTER
                    + p.getDescription() + DELIMITTER
                    + p.getImageLink() + DELIMITTER
                    + p.getPrice() + DELIMITTER
                    + p.getStars() + DELIMITTER
                    + p.getSeller()
                );
            }
            System.out.println("File already generated at path: " + path.toAbsolutePath());
            
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
        
    public static void main(String[] args) {
        export(null);
    }
}

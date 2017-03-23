/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LunchInventory;

/**
 *
 * @author ctarbet
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.io.File;

public class ReportGenerator {

    Document document;
    String fileName;
    int lowQuantityLimit;

    ReportGenerator() {
        System.out.println(new File("c:\\LunchReports").mkdir());
        fileName = "c:\\LunchReports\\Report.pdf";
        lowQuantityLimit = 5;
    }

    public void setFileName(String newFileName) {
        fileName = newFileName;
    }

    public void setLowQuantity(int newLowQuantityLimit) {
        lowQuantityLimit = newLowQuantityLimit;
    }

    public void generateReport(ResultSet rs, boolean typeFlag, boolean locationFlag) {
        try {
            PdfPTable table = new PdfPTable(5);

            int[] a = {10, 10, 45, 15, 20};

            table.setWidths(a);

            table.addCell("ID");
            table.addCell("Stock");
            table.addCell("Name");
            table.addCell("Price");
            table.addCell("Category");

            try {
                //move back to first row in existing ResultSet
                rs.absolute(1);
                while (rs.next()) {
                    if (locationFlag) {
                        if (typeFlag == true && rs.getInt("hsqty") <= lowQuantityLimit) {
                            fileName = "c:\\LunchReports\\HSReportLow.pdf";
                            table.addCell(rs.getString("id"));
                            table.addCell(rs.getString("hsqty"));
                            table.addCell(rs.getString("itemName"));
                            table.addCell(new DecimalFormat("$#,###,###,##0.00").format(rs.getDouble("price")));
                            table.addCell(rs.getString("categoryName"));
                        } else if (!typeFlag) {
                            fileName = "c:\\LunchReports\\HSReportAll.pdf";
                            table.addCell(rs.getString("id"));
                            table.addCell(rs.getString("hsqty"));
                            table.addCell(rs.getString("itemName"));
                            table.addCell(new DecimalFormat("$#,###,###,##0.00").format(rs.getDouble("price")));
                            table.addCell(rs.getString("categoryName"));
                        } else {
                            //don't add item   
                        }
                    } else {
                        if (typeFlag == true && rs.getInt("elqty") <= lowQuantityLimit) {
                            fileName = "c:\\LunchReports\\ELReportLow.pdf";
                            table.addCell(rs.getString("id"));
                            table.addCell(rs.getString("elqty"));
                            table.addCell(rs.getString("itemName"));
                            table.addCell(new DecimalFormat("$#,###,###,##0.00").format(rs.getDouble("price")));
                            table.addCell(rs.getString("categoryName"));
                        } else if (!typeFlag) {
                            fileName = "c:\\LunchReports\\ELReportAll.pdf";
                            table.addCell(rs.getString("id"));
                            table.addCell(rs.getString("elqty"));
                            table.addCell(rs.getString("itemName"));
                            table.addCell(new DecimalFormat("$#,###,###,##0.00").format(rs.getDouble("price")));
                            table.addCell(rs.getString("categoryName"));
                        } else {
                            //don't add item
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception writing PDF table: " + e + " -- " + e.getMessage());
            }
            document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            document.add(new Paragraph("Lunch Inventory Report\n\n"));
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            System.out.println("DocumentException " + e + " -- " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException " + e + " -- " + e.getMessage());
        }
    }
}

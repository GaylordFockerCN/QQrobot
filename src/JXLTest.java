
import java.io.File;

import jxl.Sheet;
import jxl.Workbook;


public class JXLTest {

	public static void main(String[] args) {
		try {
			Workbook book = Workbook.getWorkbook(new File("mark.xls"));
			Sheet sheet = book.getSheet(0);
			for(int i=0;i<820;i++) {
				if(sheet.getCell(1, i).getContents().equals("ºéêÅÁÁ")) {
					for(int j = 0;j<sheet.getColumns();j++) {
						System.out.println(sheet.getCell(j, 1).getContents()+":"+sheet.getCell(j, i).getContents());
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}

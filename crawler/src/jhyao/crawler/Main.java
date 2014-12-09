package jhyao.crawler;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	private int count;//number of books to be stored
	private Set<String> visited;//visited urls
	private static Database db = new Database();
	
	public static void main(String[] args){
		//entry url
		String host = "http://newyork.craigslist.org/search/bka";
		int count = 1000;
		Main crawler = new Main(count);
		try {
			//delete all records initially
			db.execute("TRUNCATE books;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		crawler.processPage(host);
	}
	
	public Main(int count){
		this.count = count;
		this.visited = new HashSet<>();
	}
	
	private void processPage(String URL) {
		try {
			//get page
			Document doc = Jsoup.connect(URL).timeout(5000).get();
			
			System.out.println("parsing:"+URL);
			
			//parse the page to get book title and price
			Elements books = doc.select("span[class=txt]");
			Elements titles = null;
			Elements prices = null;
			String sql = "insert into `crawler`.`books` (`title`,`price`) values (?,?)";
			PreparedStatement stmt = db.conn.prepareStatement(sql);
			for(Element book: books){
				//task completed and return
				if(count==0) return;
				count--;
				
				String title="";
				double price=0.0;
				//book title
				titles = book.select("a[class=hdrlnk]");
				if(!titles.isEmpty()) title = titles.first().text();
				
				//book price
				prices = book.select("span[class=price]");
				if(!prices.isEmpty()) price = Double.parseDouble(prices.first().text().substring(1));
				
				//insert into database
				stmt.setString(1, title);
				stmt.setDouble(2, price);
				stmt.execute();
			}
				
			//get next page
			Elements nexts = doc.select("a[class=button next]");
			if(!nexts.isEmpty()){
				//check visited to avoid duplicate
				String next = nexts.first().attr("abs:href");
				if(!visited.contains(next)){
					visited.add(next);
					//sleep 1 second to avoid lock out from the host server
					Thread.sleep(1000);
					processPage(next);
				}
			}
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}

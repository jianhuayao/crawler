package jhyao.crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	private int count;//number of books to be stored
	private Set<String> visited;//visited urls
	
	public static void main(String[] args){
		//entry url
		String host = "http://newyork.craigslist.org/search/bka";
		int count = 1000;
		Main crawler = new Main(count);
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
				
				System.out.println(count+":"+title+" "+price);
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
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}

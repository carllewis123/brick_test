package com.brick;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.brick.model.Product;
import com.brick.service.WebScraperService;

public class ProductTest {
	@Test
    public void testGetTotalProduct() throws Exception
    {
		List<Product> resultPg1 = WebScraperService.retrieveProductsAtPageTest(1);
		assertEquals(80,resultPg1.size());
		
		List<Product> resultPg2 = WebScraperService.retrieveProductsAtPageTest(2);
		assertEquals(100,resultPg2.size() + resultPg1.size()); 
    }
}

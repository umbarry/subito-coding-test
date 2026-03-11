//package com.subito.subitocodingtest;
//
//import com.subito.subitocodingtest.dto.CreateOrderRequest;
//import com.subito.subitocodingtest.dto.OrderResponse;
////import com.subito.subitocodingtest.dto.ShippingInfoRequest;
////import com.subito.subitocodingtest.dto.UserInfoRequest;
//import com.subito.subitocodingtest.exception.ResourceNotFoundException;
//import com.subito.subitocodingtest.model.Product;
//import com.subito.subitocodingtest.repository.OrderRepository;
//import com.subito.subitocodingtest.repository.ProductRepository;
////import com.subito.subitocodingtest.repository.UserInfoRepository;
////import com.subito.subitocodingtest.repository.ShippingInfoRepository;
//import com.subito.subitocodingtest.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class SubitoCodingTestApplicationTests {
//	@Autowired
//	private OrderService orderService;
//
//	@Autowired
//	private ProductRepository productRepository;
//
//	@Autowired
//	private OrderRepository orderRepository;
//
//	@Autowired
//	private UserInfoRepository userInfoRepository;
//
//	@Autowired
//	private ShippingInfoRepository shippingInfoRepository;
//
//	@BeforeEach
//	void setUp() {
//		orderRepository.deleteAll();
//		userInfoRepository.deleteAll();
//		shippingInfoRepository.deleteAll();
//		productRepository.deleteAll();
//		// Create test products
//		Product p1 = new Product(null, "Laptop", new BigDecimal("1000.00"), new BigDecimal("22"), 10, null);
//		Product p2 = new Product(null, "Mouse", new BigDecimal("25.00"), new BigDecimal("22"), 50, null);
//		productRepository.saveAll(Arrays.asList(p1, p2));
//	}
//
//	private Long getProductIdByName(String name) {
//		return productRepository.findAll().stream()
//				.filter(p -> p.getName().equals(name))
//				.findFirst()
//				.map(Product::getId)
//				.orElseThrow();
//	}
//
//	private CreateOrderRequest createTestOrderRequest(Long productId, Integer quantity) {
//		CreateOrderRequest request = new CreateOrderRequest();
//		CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest(productId, quantity);
//		//request.setItems(Arrays.asList(item));
//
//		UserInfoRequest userInfo = UserInfoRequest.builder()
//				.firstName("John")
//				.lastName("Doe")
//				.email("john.doe@example.com")
//				.phoneNumber("1234567890")
//				.build();
//		request.setUserInfo(userInfo);
//
//		ShippingInfoRequest shippingInfo = ShippingInfoRequest.builder()
//				.street("123 Main St")
//				.city("New York")
//				.postalCode("10001")
//				.country("USA")
//				.build();
//		request.setShippingInfo(shippingInfo);
//
//		return request;
//	}
//
//	@Test
//	void contextLoads() {
//	}
//
//	@Test
//	void testCreateOrder() {
//		Long laptopId = getProductIdByName("Laptop");
//		Long mouseId = getProductIdByName("Mouse");
//
//		CreateOrderRequest request = new CreateOrderRequest();
//		CreateOrderRequest.OrderItemRequest item1 = new CreateOrderRequest.OrderItemRequest(laptopId, 1);
//		CreateOrderRequest.OrderItemRequest item2 = new CreateOrderRequest.OrderItemRequest(mouseId, 2);
//		//request.setItems(Arrays.asList(item1, item2));
//
//		UserInfoRequest userInfo = UserInfoRequest.builder()
//				.firstName("John")
//				.lastName("Doe")
//				.email("john.doe@example.com")
//				.phoneNumber("1234567890")
//				.build();
//		request.setUserInfo(userInfo);
//
//		ShippingInfoRequest shippingInfo = ShippingInfoRequest.builder()
//				.street("123 Main St")
//				.city("New York")
//				.postalCode("10001")
//				.country("USA")
//				.build();
//		request.setShippingInfo(shippingInfo);
//
//		OrderResponse response = orderService.createOrder(request);
//
//		assertNotNull(response.getId());
//		assertEquals(2, response.getItems().size());
//		assertTrue(response.getTotalPrice().compareTo(BigDecimal.ZERO) > 0);
//		assertTrue(response.getTotalVat().compareTo(BigDecimal.ZERO) > 0);
//		assertTrue(response.getGrandTotal().compareTo(BigDecimal.ZERO) > 0);
//	}
//
//	@Test
//	void testOrderCalculations() {
//		Long laptopId = getProductIdByName("Laptop");
//		CreateOrderRequest request = createTestOrderRequest(laptopId, 1);
//		OrderResponse response = orderService.createOrder(request);
//
//		// Price should be 1000.00
//		assertEquals(new BigDecimal("1000.00"), response.getTotalPrice());
//		// VAT should be 1000.00 * 0.22 = 220.00
//		assertEquals(new BigDecimal("220.00"), response.getTotalVat());
//		// Grand total should be 1220.00
//		assertEquals(new BigDecimal("1220.00"), response.getGrandTotal());
//	}
//
//	@Test
//	void testGetOrder() {
//		Long laptopId = getProductIdByName("Laptop");
//		CreateOrderRequest request = createTestOrderRequest(laptopId, 1);
//		OrderResponse created = orderService.createOrder(request);
//		OrderResponse retrieved = orderService.getOrder(created.getId());
//
//		assertEquals(created.getId(), retrieved.getId());
//		assertEquals(created.getTotalPrice(), retrieved.getTotalPrice());
//		assertEquals(created.getTotalVat(), retrieved.getTotalVat());
//	}
//
//	@Test
//	void testOrderNotFound() {
//		assertThrows(ResourceNotFoundException.class, () -> {
//			orderService.getOrder(999L);
//		});
//	}
//
//	@Test
//	void testProductNotFound() {
//		CreateOrderRequest request = new CreateOrderRequest();
//		CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest(999L, 1);
//		//request.setItems(Arrays.asList(item));
//
//		UserInfoRequest userInfo = UserInfoRequest.builder()
//				.firstName("John")
//				.lastName("Doe")
//				.email("john.doe@example.com")
//				.phoneNumber("1234567890")
//				.build();
//		request.setUserInfo(userInfo);
//
//		ShippingInfoRequest shippingInfo = ShippingInfoRequest.builder()
//				.street("123 Main St")
//				.city("New York")
//				.postalCode("10001")
//				.country("USA")
//				.build();
//		request.setShippingInfo(shippingInfo);
//
//		assertThrows(ResourceNotFoundException.class, () -> {
//			orderService.createOrder(request);
//		});
//	}
//}

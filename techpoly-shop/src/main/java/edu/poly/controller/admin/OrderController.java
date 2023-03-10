package edu.poly.controller.admin;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.poly.domain.Order;
import edu.poly.domain.OrderDetail;
import edu.poly.domain.Product;
import edu.poly.repository.OrderDetailRepository;
import edu.poly.repository.OrderRepository;
import edu.poly.repository.ProductRepository;
import edu.poly.service.SendMailService;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	OrderDetailRepository orderDetailRepository;
	
	@Autowired
	SendMailService sendMailService;
	
	@Autowired
	ProductRepository productRepository;

	@RequestMapping("")
	public ModelAndView order(ModelMap model) {

		Page<Order> listO = orderRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderId")));

		model.addAttribute("orders", listO);
		// set active front-end
		model.addAttribute("menuO", "menu");
		return new ModelAndView("/admin/order");
	}

	@RequestMapping("/page")
	public ModelAndView page(ModelMap model, @RequestParam("page") Optional<Integer> page,
			@RequestParam("size") Optional<Integer> size, @RequestParam("filter") Optional<Integer> filter) {
		int currentPage = page.orElse(0);
		int pageSize = size.orElse(5);
		int filterPage = filter.orElse(0);

		Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "orderId"));
		Page<Order> listO = null;
		if (filterPage == 0) {
			listO = orderRepository.findAll(pageable);
		} else if (filterPage == 1) {
			pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "order_id"));
			listO = orderRepository.findByStatus(0, pageable);
		} else if (filterPage == 2) {
			pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "order_id"));
			listO = orderRepository.findByStatus(1, pageable);
		} else if (filterPage == 3) {
			pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "order_id"));
			listO = orderRepository.findByStatus(2, pageable);
		} else if (filterPage == 4) {
			pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "order_id"));
			listO = orderRepository.findByStatus(3, pageable);
		} else if (filterPage == 5) {
			pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "amount"));
			listO = orderRepository.findAll(pageable);
		}

		model.addAttribute("filter", filterPage);
		model.addAttribute("page", currentPage);
		model.addAttribute("orders", listO);
		// set active front-end
		model.addAttribute("menuO", "menu");
		return new ModelAndView("/admin/order");
	}

	@RequestMapping("/search")
	public ModelAndView search(ModelMap model, @RequestParam("id") String id) {
		Page<Order> listO = null;
		if (id == null || id.equals("") || id.equalsIgnoreCase("null")) {
			listO = orderRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderId")));
		} else {
			listO = orderRepository.findByorderId(Integer.valueOf(id), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderId")));
		}

		model.addAttribute("id", id);
		model.addAttribute("orders", listO);
		// set active front-end
		model.addAttribute("menuO", "menu");
		return new ModelAndView("/admin/order");
	}

	@RequestMapping("/cancel/{order-id}")
	public ModelAndView cancel(ModelMap model, @PathVariable("order-id") int id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();
		oReal.setStatus((short) 3);
		orderRepository.save(oReal);
		
		sendMailAction(oReal, "B???n ???? b??? hu??? 1 ????n h??ng t??? TechPoly Shop!",
				"Ch??ng t??i r???t ti???c!", "Th??ng b??o hu??? ????n h??ng!");
		
		return new ModelAndView("forward:/admin/orders", model);
	}

	@RequestMapping("/confirm/{order-id}")
	public ModelAndView confirm(ModelMap model, @PathVariable("order-id") int id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();
		oReal.setStatus((short) 1);
		orderRepository.save(oReal);
		
		sendMailAction(oReal, "B???n c?? 1 ????n h??ng ??? TechPoly Shop ???? ???????c x??c nh???n!",
				"Ch??ng t??i s??? s???m giao h??ng cho b???n!", "Th??ng b??o ????n h??ng ???? ???????c x??c nh???n!");
		
		return new ModelAndView("forward:/admin/orders", model);
	}

	@RequestMapping("/delivered/{order-id}")
	public ModelAndView delivered(ModelMap model, @PathVariable("order-id") int id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();
		oReal.setStatus((short) 2);
		orderRepository.save(oReal);
		
		Product p = null;
		List<OrderDetail> listDe = orderDetailRepository.findByOrderId(id);
		for(OrderDetail od : listDe) {
			p = od.getProduct();
			p.setQuantity(p.getQuantity()-od.getQuantity());
			productRepository.save(p);
		}
		
		sendMailAction(oReal, "B???n c?? 1 ????n h??ng ??? TechPoly Shop ???? thanh to??n th??nh c??ng!",
				"Ch??ng t??i c??m ??n b???n v?? ???? ???ng h??? TechPoly Shop!", "Th??ng b??o thanh to??n th??nh c??ng!");
		
		return new ModelAndView("forward:/admin/orders", model);
	}

	@RequestMapping("/detail/{order-id}")
	public ModelAndView detail(ModelMap model, @PathVariable("order-id") int id) {

		List<OrderDetail> listO = orderDetailRepository.findByOrderId(id);

		model.addAttribute("amount", orderRepository.findById(id).get().getAmount());
		model.addAttribute("orderDetail", listO);
		model.addAttribute("orderId", id);
		// set active front-end
		model.addAttribute("menuO", "menu");
		return new ModelAndView("/admin/detail", model);
	}
	

	// format currency
	public String format(String number) {
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");

		return formatter.format(Double.valueOf(number)) + " VN??";
	}

	// sendmail
		public void sendMailAction(Order oReal, String status, String cmt, String notifycation) {
			List<OrderDetail> list = orderDetailRepository.findByOrderId(oReal.getOrderId());
			System.out.println(oReal.getOrderId());

			StringBuilder stringBuilder = new StringBuilder();
			int index = 0;
			stringBuilder.append("<h3>Xin ch??o " + oReal.getCustomer().getName() + "!</h3>\r\n" + "    <h4>" + status + "</h4>\r\n"
					+ "    <table style=\"border: 1px solid gray;\">\r\n"
					+ "        <tr style=\"width: 100%; border: 1px solid gray;\">\r\n"
					+ "            <th style=\"border: 1px solid gray;\">STT</th>\r\n"
					+ "            <th style=\"border: 1px solid gray;\">T??n s???n ph???m</th>\r\n"
					+ "            <th style=\"border: 1px solid gray;\">S??? l?????ng</th>\r\n"
					+ "            <th style=\"border: 1px solid gray;\">????n gi??</th>\r\n" + "        </tr>");
			for (OrderDetail oD : list) {
				index++;
				stringBuilder.append("<tr>\r\n" + "            <td style=\"border: 1px solid gray;\">" + index + "</td>\r\n"
						+ "            <td style=\"border: 1px solid gray;\">" + oD.getProduct().getName() + "</td>\r\n"
						+ "            <td style=\"border: 1px solid gray;\">" + oD.getQuantity() + "</td>\r\n"
						+ "            <td style=\"border: 1px solid gray;\">" + format(String.valueOf(oD.getUnitPrice()))
						+ "</td>\r\n" + "        </tr>");
			}
			stringBuilder.append("\r\n" + "    </table>\r\n" + "    <h3>T???ng ti???n: "
					+ format(String.valueOf(oReal.getAmount())) + "</h3>\r\n" + "    <hr>\r\n" + "    <h5>" + cmt
					+ "</h5>\r\n" + "    <h5>Ch??c b???n 1 ng??y t???t l??nh!</h5>");

			sendMailService.queue(oReal.getCustomer().getEmail().trim(), notifycation, stringBuilder.toString());
		}
}

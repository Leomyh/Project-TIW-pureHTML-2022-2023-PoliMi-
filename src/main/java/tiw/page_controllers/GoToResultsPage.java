package tiw.page_controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import tiw.beans.ChronologyManager;
import tiw.beans.Product;
import tiw.beans.User;

@WebServlet("/GoToResultsPage")
public class GoToResultsPage extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	public void init() {
		ServletContext context = getServletContext();

		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");

		try {
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");

			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Session Error");
		}
		String keyword = (String) session.getAttribute("keyword");
		List<Product> listProducts = (List<Product>) session.getAttribute("products");

		String this_code = (String) session.getAttribute("this_code");
		// User selected a product
		if (this_code != null) {
			// Search the selected product in results product
			for (Product x : listProducts) {
				if (x.getCode().equals(this_code)) {
					boolean value = x.isSelected();
					x.setSelected(!value);
					// Update Chronology
					if (!ChronologyManager.addProductToLastSeen(user.getEmail(), x)) {
						ChronologyManager.getMyUserChronology(connection, user.getEmail());
						ChronologyManager.addProductToLastSeen(user.getEmail(), x);
					}

					session.removeAttribute("this_code");
					continue;
				}
				// Set Selected Parameter to false
				x.setSelected(false);
			}
		}

		String path = "/WEB-INF/Results.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("user", user);

		if (listProducts != null) {
			ctx.setVariable("products", listProducts);
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Session Error");
		}
		if (keyword != null) {
			ctx.setVariable("keyword", keyword);
		} else {
			ctx.setVariable("keyword", "unknown");
		}
		ctx.removeVariable("this_code");

		templateEngine.process(path, ctx, response.getWriter());
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

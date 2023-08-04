package tiw.page_controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import tiw.dao.UserDao;

@WebServlet("/GoToRegistration")
public class GoToRegistration extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public GoToRegistration() {
	super();
    }

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	String email = request.getParameter("email_reg");
	String password = request.getParameter("password_reg");
	String name = request.getParameter("name_reg");
	String surname = request.getParameter("surname_reg");
	String address = request.getParameter("address_reg");
	String cardNumber = request.getParameter("card_number_reg");
	String cardMonth = request.getParameter("month_reg");
	String cardYear = request.getParameter("year_reg");
	String cardCvv = request.getParameter("card_cvv_reg");
	String confirmPassword = request.getParameter("password_conf_reg");

	String cardExpiration = cardYear + "-" + cardMonth + "-01";
	Map<String, String> errorMap = new HashMap<>();

	// email too long
	if (email.length() >= 45)
	    errorMap.put("email_reg", "Email must be at most 45 characters");
	if (email == null || email.isEmpty() || !isValidEmail(email))
	    errorMap.put("email_reg", "Invalid email format");

	// name too long
	if (name.length() >= 45)
	    errorMap.put("name_reg", "Name must be at most 45 characters");
	// name incorrect input
	if (!name.matches("[a-zA-Z]+") || name == null || name.isEmpty())
	    errorMap.put("name_reg", "Name must contain only letters");

	// surname is too long
	if (surname.length() >= 45)
	    errorMap.put("surname_reg", "Surname must be at most 45 characters");
	// surname incorrect input
	if (!surname.matches("[a-zA-Z]+") || surname == null || surname.isEmpty())
	    errorMap.put("surname_reg", "Surname must contain only letters");

	// password too long
	if (password.length() >= 45 || password == null || password.isEmpty())
	    errorMap.put("password_reg", "Password must be at most 45 characters");
	// password confirmed
	if (!password.equals(confirmPassword) || confirmPassword == null || confirmPassword.isEmpty())
	    errorMap.put("password_conf_reg", "Confirmed Password do not correspond");

	// address too long
	if (address.length() >= 45 || address == null || address.isEmpty())
	    errorMap.put("address_reg", "Address must be at most 45 characters");

	// card must be 16 number type
	if (!isValidCardNumber(cardNumber) || cardNumber == null || cardNumber.isEmpty()) {
	    errorMap.put("card_number_reg", "Card number must be a 16-digit number");
	}

	// Check if month e/o year of card are selected
	if (cardMonth == null || cardYear == null || cardMonth.isEmpty() || cardYear.isEmpty())
	    errorMap.put("month_reg", "Please selected expire month e/o year of the card ");
	// card CVV must be 3 number type
	if (!isValidCardCvv(cardCvv) || cardCvv == null || cardCvv.isEmpty()) {
	    errorMap.put("card_cvv_reg", "Card cvv must be a 3-digit number");
	}

	if (!errorMap.isEmpty()) {
	    String path = "RegAccount.html";
	    ServletContext servletContext = getServletContext();
	    final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	    ctx.setVariable("errorMap", errorMap);
	    templateEngine.process(path, ctx, response.getWriter());
	    return;
	}

	UserDao userDao = new UserDao(connection);
	boolean result = false;
	try {
	    result = userDao.registerAccount(email, name, surname, password, address, cardNumber, cardExpiration,
		    cardCvv);
	    ServletContext servletContext = getServletContext();
	    final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	    if (result == true) {
		// Go to Index page
		String path = getServletContext().getContextPath() + "/SuccessfulReg.html";
		response.sendRedirect(path);
	    } else {
		String path = "RegAccount.html";
		String error = "Account is registered, please try login or change another one";
		ctx.setVariable("errorReg", error);
		templateEngine.process(path, ctx, response.getWriter());
		return;

	    }
	} catch (SQLException e) {
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

    }

    public boolean isValidEmail(String email) {
	String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	Pattern pattern = Pattern.compile(emailRegex);
	return pattern.matcher(email).matches();
    }

    public boolean isValidCardNumber(String cardNumber) {
	String cardNumberRegex = "\\d{16}";
	return cardNumber.matches(cardNumberRegex);
    }

    public boolean isValidCardCvv(String cvvNumber) {
	String cvvNumberRegex = "\\d{3}";
	return cvvNumber.matches(cvvNumberRegex);
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

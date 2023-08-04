package tiw.utilities;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tiw.beans.PreOrder;
import tiw.beans.Product;
import tiw.beans.Supplier;
import tiw.beans.SupplierOffer;

public class UserCart {
	public HashMap<String, Supplier> cartMap = new HashMap<>();

	public UserCart() {
	}

	public UserCart(HashMap<String, Supplier> cartMap) {
		this.cartMap = cartMap;
	}

	/**
	 * Control if the PreOrder has reached his quantity limit Check
	 * PreOrder.totalQuantity;
	 * 
	 * @param sup_code supplier code
	 * @param quantity product quantity
	 * @return true if there is still slot available for the supplier
	 */
	public boolean checkTotalQuantity(String sup_code, int quantity) {
		if (!cartMap.containsKey(sup_code)) {
			return true;
		}
		if ((cartMap.get(sup_code).getPreOrder_ref().totalQuantity + quantity) < 1000) {
			return true;
		}

		return false;
	}

	/**
	 * Add PreOrder in User Cart
	 */
	public void addPreOrder(SupplierOffer supOff, Product product, int quantity) {
		String sup_code = supOff.supplier.getCode();
		String product_code = product.getCode();
		float price = supOff.offer.getPrice();
		Supplier sup = supOff.supplier;
		if (!cartMap.containsKey(sup_code)) {

			sup.setPreOrder_ref(createPreOrder(sup, product_code, quantity, price));
			cartMap.put(sup_code, sup);

		} else {
			// Supplier exist in the cart, add new Product Set
			PreOrder toUpdate = cartMap.get(sup_code).getPreOrder_ref();
			toUpdate.updatePreOrder(product_code, quantity, price);

		}

	}

	/**
	 * Create a PreOrder with just a product type (x quantity)
	 * 
	 * @param product_code product code
	 * @param quantity     product quantity
	 * @param price        product price
	 * @return PreOrder object
	 */
	public PreOrder createPreOrder(Supplier sup, String product_code, int quantity, float price) {
		PreOrder preOrder = new PreOrder(sup);
		PriceQuantity data = new PriceQuantity(price, quantity);
		preOrder.preOrderMap.put(product_code, data);
		preOrder.updateTotalQuantity();
		preOrder.updateTotalCost();
		preOrder.updateShippingCost();

		return preOrder;
	}

	/**
	 * Remove PreOrder from the cartMap
	 * 
	 * @param response
	 * @param req
	 * @param supplier_code
	 * @return
	 */
	public boolean removeCartPreOrder(HttpServletResponse response, HttpServletRequest req, String supplier_code) {
		if (cartMap.containsKey(supplier_code)) {
			cartMap.remove(supplier_code);
			return true;

		}
		return false;
	}

}

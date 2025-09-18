package gui.shop;

import entity.shop.Coupon;
import entity.shop.Order;
import entity.shop.OrderItem;
import entity.shop.Product;
import entity.shop.SalePromotion;
import service.shop.*;
import service.shop.impl.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店管理界面的GUI实现。
 * 此类只负责UI展示，所有业务逻辑委托给AdminLogicHandler处理。
 */
public class AdminView extends JFrame {
    private final Runnable onExitCallback;

    // --- UI组件 ---
    private JPanel productPanel;
    private JList<String> categoryList;
    
    private JPanel couponPanel;
    private JComboBox<String> filterComboBox;
    private JPanel SalePromotionPanel;
    private JComboBox<String> filterComboBoxforC;
    private DefaultTableModel tableModel;
    private ShopService shopService = new ShopServiceImpl();
	private CouponService couponService = new CouponServiceImpl();
	private ProductService productService = new ProductServiceImpl();
	private SalePromotionService salePromotionService = new SalePromotionServiceImpl();

    // --- 数据模型 ---
    private Map<String, ArrayList<Product>> productMap = new HashMap<>();
    
    private List<Coupon> allCouponList = new ArrayList<>();
    
	private List<SalePromotion> allSaleProList = new ArrayList<>();

	private JPanel saleProPanel;

    public AdminView(Runnable onExitCallback) {
        this.onExitCallback = onExitCallback;

        setTitle("商店管理");
        // 设置窗口关闭操作
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onExitCallback != null) {
                    onExitCallback.run();
                }
            }
        });
        setSize(400, 400);
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(245, 245, 245));
        setContentPane(contentPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 10, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton aButton = new JButton("管理商品信息");
        aButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        aButton.addActionListener(e -> createAWindow());

        JButton bButton = new JButton("管理优惠券信息");
        bButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        bButton.addActionListener(e -> createBWindow());

        JButton cButton = new JButton("管理活动信息");
        cButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cButton.addActionListener(e -> createCWindow());

        JButton dButton = new JButton("查看订单");
        dButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        dButton.addActionListener(e -> createDWindow());

        buttonsPanel.add(aButton);
        buttonsPanel.add(bButton);
        buttonsPanel.add(cButton);
        buttonsPanel.add(dButton);
        contentPanel.add(buttonsPanel);
    }
    
    private void createAWindow() {
        JFrame aWindow = new JFrame("管理商品信息");
        aWindow.setSize(600, 650);
        aWindow.setLocationRelativeTo(this);
        aWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Color backgroundColor = new Color(245, 245, 245);
        Color panelColor = Color.WHITE;
        
        productMap = productService.getProductsGroupedByCategory();
        List<Product> lowStockProducts = new ArrayList<>();
        for (List<Product> products : productMap.values()) {
            for (Product product : products) {
                if (product.getStockAmount() <= 5 && product.getStockAmount() > 0) {
                    lowStockProducts.add(product); } }
        }
        if (!lowStockProducts.isEmpty()) {
            showLowStockWarning(lowStockProducts);
        }
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(backgroundColor);
        aWindow.setContentPane(contentPanel);
        
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (String category : productMap.keySet()) {
            listModel.addElement(category);
        }
        categoryList = new JList<String>(listModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        categoryList.setFixedCellHeight(30);
        categoryList.setBackground(panelColor);
        categoryList.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        categoryList.setSelectedIndex(0);
        categoryList.addListSelectionListener(new ListSelectionListener() { 
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) updateProductPanel(categoryList.getSelectedValue());
            }
        });

        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBackground(backgroundColor);
        JLabel categoryTitle = new JLabel("商品类别");
        categoryTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        categoryTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        categoryPanel.add(categoryTitle, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setPreferredSize(new Dimension(110, 0));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        categoryPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(categoryPanel, BorderLayout.WEST);
        
        productPanel = new JPanel();
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        productPanel.setBackground(backgroundColor);
        productPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        JScrollPane productScrollPane = new JScrollPane(productPanel);
        productScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        productScrollPane.getViewport().setBackground(backgroundColor);
        contentPanel.add(productScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(backgroundColor);
        JButton addStockBtn = new JButton("增加库存");
        JButton addProductBtn = new JButton("添加商品");
        JButton deleteProductBtn = new JButton("删除商品");
        addStockBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddStockDialog();
            }
        });
        addProductBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	showAddProductDialog();
            }
        });
        deleteProductBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	showDeleteProductDialog();
            }
        });
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.setBackground(backgroundColor);
        JComboBox<String> searchTypeComboBox = new JComboBox<>();
        searchTypeComboBox.addItem("按Id搜索");
        searchTypeComboBox.addItem("按名称搜索");
        searchTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchTypeComboBox.setPreferredSize(new Dimension(80, 25));
        searchPanel.add(searchTypeComboBox, BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setPreferredSize(new Dimension(180, 25));
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchButton.setPreferredSize(new Dimension(60, 25));
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        buttonPanel.add(addStockBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(addProductBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(deleteProductBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(searchPanel);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        updateProductPanel(categoryList.getSelectedValue());
        aWindow.setVisible(true);
    }
    private void updateProductPanel(String category) {
        productPanel.removeAll();
        if (category == null) return;
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        productPanel.add(categoryLabel);
        ArrayList<Product> products = productMap.get(category);
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                productPanel.add(createProductItemPanel(product));
                productPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        } else {
            JLabel noProductsLabel = new JLabel("该类别暂无商品");
            noProductsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            noProductsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            noProductsLabel.setForeground(Color.GRAY);
            productPanel.add(noProductsLabel);
        }
        productPanel.add(Box.createVerticalGlue());
        productPanel.revalidate();
        productPanel.repaint();
    }
    private JPanel createProductItemPanel(Product product) {
        Color panelColor = Color.WHITE;
        Color borderColor = new Color(220, 220, 220);
        Color buttonColor = new Color(70, 130, 180);
        Color buttonTextColor = Color.WHITE;
        
        JPanel panel = new JPanel(new BorderLayout(5, 5)); 
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 150));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(panelColor);
        leftPanel.setPreferredSize(new Dimension(90, 0));
        
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(panelColor);
        imageContainer.setPreferredSize(new Dimension(80, 80));
        imageContainer.setMaximumSize(new Dimension(80, 80));
        imageContainer.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        try {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();
            double ratio = (double) width / height;
            
            int newWidth, newHeight;
            if (ratio > 1) {
                newWidth = 75;
                newHeight = (int) (75 / ratio);
            } else {
                newHeight = 75;
                newWidth = (int) (75 * ratio);
            }
            
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                newWidth, newHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("图片加载失败");
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        leftPanel.add(imageContainer);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        
        JButton modifyButton = new JButton("修改信息");
        modifyButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        modifyButton.setBackground(buttonColor);
        modifyButton.setForeground(buttonTextColor);
        modifyButton.setFocusPainted(false);
        modifyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        modifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        modifyButton.setMaximumSize(new Dimension(75, 20));
        modifyButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	modifyProductInfo(product);
	        }
	    });
        leftPanel.add(modifyButton);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(panelColor);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        JLabel idnameLabel = new JLabel(product.getProductId() + " - " 
                  + product.getName());
        idnameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        idnameLabel.setForeground(new Color(100, 100, 100));
        idnameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(idnameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        JLabel pwLabel = new JLabel("价格：" + String.format("¥%.2f", product.getPrice()) + 
                " 重量：" + product.getWeight() + "kg");
        pwLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        pwLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(pwLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        JLabel sLabel = new JLabel("本月售出：" + product.getSoldAmount() + 
                "  库存：" + product.getStockAmount());
        sLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sLabel.setForeground(new Color(100, 100, 100));
        sLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(sLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        JLabel descLabel = new JLabel("说明：" + product.getDescription());
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(new Color(80, 80, 80));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        JLabel specLabel = new JLabel("规格：" + (product.getSpecification() == null ? "无" : 
        	product.getSpecification()) + "  可选：" + 
        		(product.getChoice() == null ? "无" : product.getChoice()));
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        specLabel.setForeground(new Color(80, 80, 80));
        specLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(specLabel);
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }
    private void modifyProductInfo(Product product) {
    	JDialog modifyDialog = new JDialog((JFrame) null, "修改商品信息", true);
        modifyDialog.setSize(500, 450);
        modifyDialog.setLocationRelativeTo(null);
        modifyDialog.setLayout(new BorderLayout(10, 10));
        modifyDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("商品名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField nameField = new JTextField(product.getName());
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel priceLabel = new JLabel("价格:");
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        priceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel weightLabel = new JLabel("重量(kg):");
        weightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField weightField = new JTextField(String.valueOf(product.getWeight()));
        weightField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel descLabel = new JLabel("商品描述:");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextArea descArea = new JTextArea(product.getDescription(), 3, 20);
        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(200, 60));
        JLabel imageLabel = new JLabel("图片路径:");
        imageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField imageField = new JTextField(product.getImagePath());
        imageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JButton browseButton = new JButton("浏览...");
        browseButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));
            int result = fileChooser.showOpenDialog(modifyDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                imageField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.add(imageField, BorderLayout.CENTER);
        imagePanel.add(browseButton, BorderLayout.EAST);
        JLabel specLabel = new JLabel("规格:");
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField specField = new JTextField(product.getSpecification());
        specField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel choiceLabel = new JLabel("可选项目:");
        choiceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField choiceField = new JTextField(product.getChoice());
        choiceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(weightLabel);
        formPanel.add(weightField);
        formPanel.add(descLabel);
        formPanel.add(descScroll);
        formPanel.add(imageLabel);
        formPanel.add(imagePanel);
        formPanel.add(specLabel);
        formPanel.add(specField);
        formPanel.add(choiceLabel);
        formPanel.add(choiceField);
        mainPanel.add(formPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveProductChanges(product, nameField, priceField, weightField, 
                                  descArea, imageField, specField, choiceField)) {
                modifyDialog.dispose();
                updateProductPanel(categoryList.getSelectedValue());
            }
        });
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);
        modifyDialog.add(mainPanel, BorderLayout.CENTER);
        modifyDialog.setVisible(true);
    }
    private boolean saveProductChanges(Product product, JTextField nameField, JTextField priceField,
            JTextField weightField, JTextArea descArea, JTextField imageField,
            JTextField specField, JTextField choiceField) {
    	try {
    		double weight, price;
    		if (nameField.getText().trim().isEmpty()) {
    			JOptionPane.showMessageDialog(null, "商品名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    		try {
    			price = Double.parseDouble(priceField.getText().trim());
    			if (price <= 0) {
    	    		JOptionPane.showMessageDialog(null, "价格必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
    	    		return false; }
    		} catch (NumberFormatException e) {
    			JOptionPane.showMessageDialog(null, "价格必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
        		return false; }
    		try {
    			weight = Double.parseDouble(weightField.getText().trim());
    			if (weight <= 0) {
    	    		JOptionPane.showMessageDialog(null, "重量必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
    	    		return false; }
    		} catch (NumberFormatException e) {
    		    JOptionPane.showMessageDialog(null, "重量必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    		product.setName(nameField.getText().trim());
            product.setPrice(price);
            product.setWeight(weight);
            product.setDescription(descArea.getText().trim());
            product.setImagePath(imageField.getText().trim());
            product.setSpecification((specField.getText().trim().isEmpty() ? 
            		null : specField.getText().trim()));
            product.setChoice((choiceField.getText().trim().isEmpty() ? null : 
            		choiceField.getText().trim()));
            boolean isUpadateOnDB = productService.updateProduct(product);
            return isUpadateOnDB;
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(null, "更新商品信息时发生错误: " + e.getMessage(), 
    		       "错误", JOptionPane.ERROR_MESSAGE);
    	    return false;  }
    }
    private void showAddStockDialog() {
    	JDialog stockDialog = new JDialog((JFrame) null, "增加库存", true);
        stockDialog.setSize(200, 160);
        stockDialog.setLocationRelativeTo(null);
        stockDialog.getContentPane().setBackground(new Color(245, 245, 245));
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel idLabel = new JLabel("商品ID:");
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField idField = new JTextField();
        idField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel amountLabel = new JLabel("增加数量:");
        amountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField amountField = new JTextField();
        amountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton confirmButton = new JButton("确认");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        buttonPanel.add(confirmButton, BorderLayout.CENTER);
        contentPanel.add(idLabel);
        contentPanel.add(idField);
        contentPanel.add(amountLabel);
        contentPanel.add(amountField);
        contentPanel.add(buttonPanel);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productId = idField.getText().trim();
                String amountText = amountField.getText().trim();
                if (productId.isEmpty() || amountText.isEmpty()) {
                    JOptionPane.showMessageDialog(stockDialog, "请输入商品Id和数量", "错误", JOptionPane.ERROR_MESSAGE);
                    return; }
                try {
                    int amount = Integer.parseInt(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(stockDialog, "数量必须为正整数", "错误", JOptionPane.ERROR_MESSAGE);
                        return; }
                    Product product = productService.getProductById(productId);
                    if (product == null) {
                        JOptionPane.showMessageDialog(stockDialog, "商品不存在", "错误", JOptionPane.ERROR_MESSAGE);
                        return; }
                    int newStock = product.getStockAmount() + amount;
                    boolean success = productService.updateStock(productId, newStock);
                    if (success) {
                        JOptionPane.showMessageDialog(stockDialog, "库存更新成功：" + product.getName() + "增加" + amount
                        		+ "库存", "成功", JOptionPane.INFORMATION_MESSAGE);
                        product.setStockAmount(newStock);
                        for (ArrayList<Product> products : productMap.values()) {
                            for (Product p : products) {
                                if (p.getProductId().equals(productId)) {
                                    p.setStockAmount(newStock); break; }
                            }
                        }
                        updateProductPanel(categoryList.getSelectedValue());
                        stockDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(stockDialog, "库存更新失败", "错误", JOptionPane.ERROR_MESSAGE); }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(stockDialog, "数量必须是整数", "错误", JOptionPane.ERROR_MESSAGE); }
            }
        });
        stockDialog.add(contentPanel);
        stockDialog.setVisible(true);
    }
    private void showAddProductDialog() {
        JDialog addDialog = new JDialog((JFrame) null, "添加商品", true);
        addDialog.setSize(480, 540);
        addDialog.setLocationRelativeTo(null);
        addDialog.setLayout(new BorderLayout(10, 10));
        addDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel idLabel = new JLabel("商品Id:");
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField idField = new JTextField();
        idField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel nameLabel = new JLabel("商品名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField nameField = new JTextField();
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel categoryLabel = new JLabel("所属类别:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField categoryField = new JTextField();
        categoryField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel priceLabel = new JLabel("价格:");
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField priceField = new JTextField();
        priceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel weightLabel = new JLabel("重量(kg):");
        weightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField weightField = new JTextField();
        weightField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel descLabel = new JLabel("商品描述:");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(200, 60));
        JLabel imageLabel = new JLabel("图片路径:");
        imageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField imageField = new JTextField();
        imageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JButton browseButton = new JButton("浏览...");
        browseButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));
            int result = fileChooser.showOpenDialog(addDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                imageField.setText(fileChooser.getSelectedFile().getAbsolutePath()); }
        });
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.add(imageField, BorderLayout.CENTER);
        imagePanel.add(browseButton, BorderLayout.EAST);
        JLabel specLabel = new JLabel("默认规格:");
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField specField = new JTextField();
        specField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel choiceLabel = new JLabel("可选规格:");
        choiceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField choiceField = new JTextField();
        choiceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel stockLabel = new JLabel("初始库存:");
        stockLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField stockField = new JTextField("0");
        stockField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(categoryLabel);
        formPanel.add(categoryField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(weightLabel);
        formPanel.add(weightField);
        formPanel.add(descLabel);
        formPanel.add(descScroll);
        formPanel.add(imageLabel);
        formPanel.add(imagePanel);
        formPanel.add(specLabel);
        formPanel.add(specField);
        formPanel.add(choiceLabel);
        formPanel.add(choiceField);
        formPanel.add(stockLabel);
        formPanel.add(stockField);
        mainPanel.add(formPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveNewProduct(idField, nameField, priceField, weightField, descArea, 
                              imageField, specField, choiceField, categoryField, stockField)) {
                productMap = productService.getProductsGroupedByCategory();
                String currentSelection = categoryList.getSelectedValue();
                DefaultListModel<String> listModel = (DefaultListModel<String>) categoryList.getModel();
                listModel.removeAllElements();
                for (String category : productMap.keySet()) {
                    listModel.addElement(category);
                }
                if (currentSelection != null && listModel.contains(currentSelection)) {
                    categoryList.setSelectedValue(currentSelection, true);
                } else if (listModel.size() > 0) {
                    categoryList.setSelectedIndex(0);
                }
                updateProductPanel(categoryList.getSelectedValue());
                addDialog.dispose();
            }
        });
        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        mainPanel.add(buttonPanel);
        addDialog.add(mainPanel, BorderLayout.CENTER);
        addDialog.setVisible(true);
    }
    private boolean saveNewProduct(JTextField idField, JTextField nameField, JTextField priceField,
            JTextField weightField, JTextArea descArea, JTextField imageField,
            JTextField specField, JTextField choiceField, JTextField categoryField,
            JTextField stockField) {
    	try {
    		String productId = idField.getText().trim();
    		String name = nameField.getText().trim();
    		String priceText = priceField.getText().trim();
    		String weightText = weightField.getText().trim();
    		String description = descArea.getText().trim();
    		String imagePath = imageField.getText().trim();
    		String specification = specField.getText().trim();
    		String choice = choiceField.getText().trim();
    		String category = categoryField.getText().trim();
    		String stockText = stockField.getText().trim();
    		if(choice.isEmpty()) choice = null;
    		if(specification.isEmpty()) specification = null;
    		if (productId.isEmpty() || name.isEmpty() || category.isEmpty() || description.isEmpty()) {
    			JOptionPane.showMessageDialog(null, "商品Id、名称、类别和说明不能为空", "错误", JOptionPane.ERROR_MESSAGE);
        		return false; }
    		Product existingProduct = productService.getProductById(productId);
    		if (existingProduct != null) {
    			JOptionPane.showMessageDialog(null, "商品Id已存在", "错误", JOptionPane.ERROR_MESSAGE);
        		return false; }
    		double price, weight;
    		int stock;
    		try {
    			price = Double.parseDouble(priceText);
        		if (price <= 0) {
        		    JOptionPane.showMessageDialog(null, "价格必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
        		    return false;  }
    		} catch (NumberFormatException e) {
    		    JOptionPane.showMessageDialog(null, "价格必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    		try {
    			weight = Double.parseDouble(weightText);
        		if (weight <= 0) {
        		    JOptionPane.showMessageDialog(null, "重量必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
        		    return false; }
    		} catch (NumberFormatException e) {
    		    JOptionPane.showMessageDialog(null, "重量必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    		try {
    			stock = Integer.parseInt(stockText);
        		if (stock < 0) {
        		    JOptionPane.showMessageDialog(null, "库存不能为负数", "错误", JOptionPane.ERROR_MESSAGE);
        		    return false; }
        		} catch (NumberFormatException e) {
        		JOptionPane.showMessageDialog(null, "库存必须是有效的整数", "错误", JOptionPane.ERROR_MESSAGE);
        		return false;
    		}
    		Product newProduct = new Product(productId, name, category, price, weight, 
    				 0, stock, description, imagePath, choice, specification);
    		boolean success = productService.addProduct(newProduct);
    		if (success) {
    		    JOptionPane.showMessageDialog(null, "商品添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
    		    return true;
    		} else {
    		    JOptionPane.showMessageDialog(null, "商品添加失败", "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    		} catch (Exception e) {
    		    JOptionPane.showMessageDialog(null, "添加商品时发生错误: " + e.getMessage(), 
    		                 "错误", JOptionPane.ERROR_MESSAGE);
    		    return false; }
    }
    private void showDeleteProductDialog() {
        JDialog deleteDialog = new JDialog((JFrame) null, "删除商品", true);
        deleteDialog.setSize(200, 150);
        deleteDialog.setLocationRelativeTo(null);
        deleteDialog.getContentPane().setBackground(new Color(245, 245, 245));
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel idLabel = new JLabel("商品Id:");
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField idField = new JTextField();
        idField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton confirmButton = new JButton("确认");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        buttonPanel.add(confirmButton, BorderLayout.CENTER);
        contentPanel.add(idLabel);
        contentPanel.add(idField);
        contentPanel.add(buttonPanel);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productId = idField.getText().trim();
                if (productId.isEmpty()) {
                    JOptionPane.showMessageDialog(deleteDialog, "请输入商品Id", "错误", JOptionPane.ERROR_MESSAGE);
                    return; }
                Product product = productService.getProductById(productId);
                if (product == null) {
                    JOptionPane.showMessageDialog(deleteDialog, "商品不存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return; }
                boolean success = productService.deleteProduct(productId);
                if (success) {
                    JOptionPane.showMessageDialog(deleteDialog, "商品删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    productMap = productService.getProductsGroupedByCategory();
                    String currentSelection = categoryList.getSelectedValue();
                    DefaultListModel<String> listModel = (DefaultListModel<String>) categoryList.getModel();
                    listModel.removeAllElements();
                    for (String category : productMap.keySet()) {
                        listModel.addElement(category);
                    }
                    if (currentSelection != null && listModel.contains(currentSelection)) {
                        categoryList.setSelectedValue(currentSelection, true);
                    } else if (listModel.size() > 0) {
                        categoryList.setSelectedIndex(0);
                    }
                    updateProductPanel(categoryList.getSelectedValue());
                    deleteDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(deleteDialog, "商品删除失败", "错误", JOptionPane.ERROR_MESSAGE); }
            }
        });
        deleteDialog.add(contentPanel);
        deleteDialog.setVisible(true);
    }
    private void performSearch(int searchType, String keyword) {
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词", "提示", JOptionPane.INFORMATION_MESSAGE);
            return; }
        try {
        	productPanel.removeAll();
            List<Product> searchResults;
            if (searchType == 0) {
                searchResults = productService.searchProductsById(keyword);
            } else { 
                searchResults = productService.searchProductsByName(keyword);
            }
            for (Product product : searchResults) {
    	        productPanel.add(createProductItemPanel(product));
    	        productPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    	    }
            if(searchResults.isEmpty()) {
            	JLabel noResultsLabel = new JLabel("没有结果");
    	        noResultsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
    	        noResultsLabel.setForeground(new Color(120, 120, 120));
    	        noResultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    	        productPanel.add(noResultsLabel);
            }
    	    productPanel.add(Box.createVerticalGlue());
    	    productPanel.revalidate();
    	    productPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "搜索过程中发生错误: " + ex.getMessage(), 
                         "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showLowStockWarning(List<Product> lowStockProducts) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("库存预警", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(200, 0, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        JLabel warningLabel = new JLabel("以下商品库存不足（≤5），请及时补货：");
        warningLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        warningLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(warningLabel, BorderLayout.CENTER);
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Product product : lowStockProducts) {
            listModel.addElement(product.getName() + " (Id: " + product.getProductId() + 
                    ", 当前库存: " + product.getStockAmount() + ")"); }
        JList<String> productList = new JList<>(listModel);
        productList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(scrollPane, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(null, panel, "库存预警", JOptionPane.WARNING_MESSAGE );
    }
    
    private void createBWindow() {
    	JFrame bWindow = new JFrame("管理优惠券信息");
        bWindow.setSize(450, 500);
        bWindow.setLocationRelativeTo(this);
        bWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Color backgroundColor = new Color(245, 245, 245);
        Color panelColor = Color.WHITE;
        
        allCouponList = couponService.getAllCouponTemplates();
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(backgroundColor);
        bWindow.setContentPane(contentPanel);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(backgroundColor);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel filterLabel = new JLabel("筛选:");
        filterLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterComboBox = new JComboBox<>();
        filterComboBox.addItem("全部优惠券");
        filterComboBox.addItem("仅显示可用");
        filterComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        
        couponPanel = new JPanel();
        couponPanel.setLayout(new BoxLayout(couponPanel, BoxLayout.Y_AXIS));
        couponPanel.setBackground(backgroundColor);
        JScrollPane couponScrollPane = new JScrollPane(couponPanel);
        couponScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        couponScrollPane.getViewport().setBackground(backgroundColor);
        contentPanel.add(couponScrollPane, BorderLayout.CENTER);
        
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.setBackground(backgroundColor);
        JComboBox<String> searchTypeComboBox = new JComboBox<>();
        searchTypeComboBox.addItem("按Id搜索");
        searchTypeComboBox.addItem("按类别搜索");
        searchTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchTypeComboBox.setPreferredSize(new Dimension(80, 25));
        searchPanel.add(searchTypeComboBox, BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setPreferredSize(new Dimension(120, 25));
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchButton.setPreferredSize(new Dimension(60, 25));
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performCouponSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performCouponSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton addCouponBtn = new JButton("添加优惠券");
        addCouponBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        buttonPanel.add(addCouponBtn, BorderLayout.WEST);
        buttonPanel.add(searchPanel, BorderLayout.EAST);
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = filterComboBox.getSelectedIndex();
                List<Coupon> filteredCoupons;
                if (selectedIndex == 0) {
                	filteredCoupons = couponService.getAllCouponTemplates();
                    allCouponList = filteredCoupons;
                } else {
                    filteredCoupons = couponService.getAvailableCouponTemplate(); }
                updateCouponPanel(filteredCoupons);
            }
        });
        addCouponBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddCouponDialog(filterComboBox);
            }
        });
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        updateCouponPanel(allCouponList);
        
        bWindow.setVisible(true);
    }
    private void updateCouponPanel(List<Coupon> coupons) {
        couponPanel.removeAll();
        if (coupons == null || coupons.isEmpty()) {
            JLabel noCouponsLabel = new JLabel("暂无优惠券");
            noCouponsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            noCouponsLabel.setForeground(Color.GRAY);
            noCouponsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            couponPanel.add(noCouponsLabel);
        } else {
            for (Coupon coupon : coupons) {
                couponPanel.add(createCouponItemPanel(coupon));
                couponPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        couponPanel.add(Box.createVerticalGlue());
        couponPanel.revalidate();
        couponPanel.repaint();
    }
    private JPanel createCouponItemPanel(Coupon coupon) {
        Color panelColor = Color.WHITE;
        Color borderColor = new Color(220, 220, 220);
        Color buttonColor = new Color(70, 130, 180);
        Color buttonTextColor = Color.WHITE;
        Color discountColor = new Color(220, 0, 0);
        
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(panelColor);
        JLabel idLabel = new JLabel("Id: " + coupon.getCouponId());
        idLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        idLabel.setForeground(new Color(100, 100, 100));
        JLabel nameLabel = new JLabel(coupon.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(new Color(70, 70, 70));
        JLabel discountLabel = new JLabel("优惠：满" + String.format("¥%.2f", coupon.getSpendMoney()) + 
                          "减" + String.format("¥%.2f", coupon.getOffMoney()));
        discountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        discountLabel.setForeground(discountColor);
        JLabel categoryLabel = new JLabel("类别：" + (coupon.getCategory() == null ? 
        		"全场可用" : coupon.getCategory()));
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryLabel.setForeground(new Color(100, 100, 100));
        String dueDate = coupon.getDueTime().toLocalDate().toString();
        String dueTime = coupon.getDueTime().toLocalTime().toString();
        JLabel dueLabel = new JLabel("到期时间：" + dueDate + " " + dueTime);
        dueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        dueLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(idLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(discountLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(dueLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(panelColor);
        JButton modifyButton = new JButton("修改信息");
        modifyButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        modifyButton.setBackground(buttonColor);
        modifyButton.setForeground(buttonTextColor);
        modifyButton.setFocusPainted(false);
        modifyButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        modifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyCouponInfo(coupon);
            }
        });
        JButton deleteButton = new JButton("删除优惠券");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deleteButton.setBackground(new Color(200, 0, 0));
        deleteButton.setForeground(buttonTextColor);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	couponService.deleteCouponTemplate(coupon.getCouponId());
            	JOptionPane.showMessageDialog(null, "优惠券删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                allCouponList = couponService.getAllCouponTemplates();
                int selectedIndex = filterComboBox.getSelectedIndex();
                List<Coupon> filteredCoupons = selectedIndex == 0 ? allCouponList : 
                	couponService.getAvailableCouponTemplate();
                updateCouponPanel(filteredCoupons);
            }
        });
        buttonPanel.add(modifyButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(deleteButton);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }
    private void modifyCouponInfo(Coupon coupon) {
        JDialog modifyDialog = new JDialog((JFrame) null, "修改优惠券信息", true);
        modifyDialog.setSize(500, 450);
        modifyDialog.setLocationRelativeTo(null);
        modifyDialog.setLayout(new BorderLayout(10, 10));
        modifyDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel nameLabel = new JLabel("优惠券名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField nameField = new JTextField(coupon.getName());
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel spendMoneyLabel = new JLabel("满减条件:");
        spendMoneyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField spendMoneyField = new JTextField(String.valueOf(coupon.getSpendMoney()));
        spendMoneyField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel offMoneyLabel = new JLabel("减金额:");
        offMoneyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField offMoneyField = new JTextField(String.valueOf(coupon.getOffMoney()));
        offMoneyField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel categoryLabel = new JLabel("适用类别:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField categoryField = new JTextField(coupon.getCategory());
        categoryField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        String dueDate = coupon.getDueTime().toLocalDate().toString();
        String dueTime = coupon.getDueTime().toLocalTime().toString();
        JLabel dueDateLabel = new JLabel("到期日期:");
        dueDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueDateField = new JTextField(dueDate);
        dueDateField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueTimeLabel = new JLabel("到期时间:");
        dueTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueTimeField = new JTextField(dueTime);
        dueTimeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(spendMoneyLabel);
        formPanel.add(spendMoneyField);
        formPanel.add(offMoneyLabel);
        formPanel.add(offMoneyField);
        formPanel.add(categoryLabel);
        formPanel.add(categoryField);
        formPanel.add(dueDateLabel);
        formPanel.add(dueDateField);
        formPanel.add(dueTimeLabel);
        formPanel.add(dueTimeField);
        
        mainPanel.add(formPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveCouponChanges(coupon, nameField, spendMoneyField, offMoneyField,
                                 categoryField, dueDateField, dueTimeField)) {
                modifyDialog.dispose();
                allCouponList = couponService.getAllCouponTemplates();
                int selectedIndex = filterComboBox.getSelectedIndex();
                List<Coupon> filteredCoupons = selectedIndex == 0 ? allCouponList : 
                	couponService.getAvailableCouponTemplate();
                updateCouponPanel(filteredCoupons);
            }
        });
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);
        modifyDialog.add(mainPanel, BorderLayout.CENTER);
        modifyDialog.setVisible(true);
    }
    private boolean saveCouponChanges(Coupon coupon, JTextField nameField, JTextField spendMoneyField,
                                     JTextField offMoneyField, JTextField categoryField, JTextField dueDateField,
                                     JTextField dueTimeField) {
        try {
            String name = nameField.getText().trim();
            String spendMoneyText = spendMoneyField.getText().trim();
            String offMoneyText = offMoneyField.getText().trim();
            String category = categoryField.getText().trim();
            if(category.isEmpty()) { category = null; }
            else {
            	List<String>allCategories = productService.getAllCategories();
            	if(!allCategories.contains(category)) {
            		JOptionPane.showMessageDialog(null, "查无此类别", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            }
            String dueDate = dueDateField.getText().trim();
            String dueTime = dueTimeField.getText().trim();
            if (name.isEmpty() || spendMoneyText.isEmpty() || offMoneyText.isEmpty() ||
            		(dueDate.isEmpty() && !dueTime.isEmpty()) || (!dueDate.isEmpty() && dueTime.isEmpty())) {
                JOptionPane.showMessageDialog(null, "缺少必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            double spendMoney, offMoney;
            try {
                spendMoney = Double.parseDouble(spendMoneyText);
                if (spendMoney <= 0) {
                    JOptionPane.showMessageDialog(null, "满减条件必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "满减条件必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            try {
                offMoney = Double.parseDouble(offMoneyText);
                if (offMoney <= 0) {
                    JOptionPane.showMessageDialog(null, "减金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
                if (offMoney >= spendMoney) {
                    JOptionPane.showMessageDialog(null, "减金额必须小于满减条件", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "减金额必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            LocalDateTime dueDateTime;
            try {
            	if(dueDate.isEmpty() && dueTime.isEmpty()) { dueDateTime = null; }
            	else {
            		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            		LocalDate date = LocalDate.parse(dueDate, dateFormatter);
            		LocalTime time = LocalTime.parse(dueTime, timeFormatter);
            		dueDateTime = LocalDateTime.of(date, time);
            	}
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "日期格式不正确，请使用yyyy-MM-dd格式", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
          
            coupon.setName(name);
            coupon.setSpendMoney(spendMoney);
            coupon.setOffMoney(offMoney);
            coupon.setCategory(category);
            coupon.setDueTime(dueDateTime);
            couponService.updateCouponTemplate(coupon);
            JOptionPane.showMessageDialog(null, "优惠券信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            return true; 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "更新优惠券信息时发生错误: " + e.getMessage(), 
                                       "错误", JOptionPane.ERROR_MESSAGE);
            return false; }
    }
    private void showAddCouponDialog(JComboBox<String> filterComboBox) {
        JDialog addDialog = new JDialog((JFrame) null, "添加优惠券", true);
        addDialog.setSize(450, 350);
        addDialog.setLocationRelativeTo(null);
        addDialog.setLayout(new BorderLayout(10, 10));
        addDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel idLabel = new JLabel("优惠券Id:");
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField idField = new JTextField();
        idField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel nameLabel = new JLabel("优惠券名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField nameField = new JTextField();
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel spendMoneyLabel = new JLabel("满减条件:");
        spendMoneyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField spendMoneyField = new JTextField();
        spendMoneyField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel offMoneyLabel = new JLabel("减金额:");
        offMoneyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField offMoneyField = new JTextField();
        offMoneyField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel categoryLabel = new JLabel("适用类别:");
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField categoryField = new JTextField();
        categoryField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueDateLabel = new JLabel("到期日期:");
        dueDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueDateField = new JTextField();
        dueDateField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueTimeLabel = new JLabel("到期时间:");
        dueTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueTimeField = new JTextField("23:59:59");
        dueTimeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(spendMoneyLabel);
        formPanel.add(spendMoneyField);
        formPanel.add(offMoneyLabel);
        formPanel.add(offMoneyField);
        formPanel.add(categoryLabel);
        formPanel.add(categoryField);
        formPanel.add(dueDateLabel);
        formPanel.add(dueDateField);
        formPanel.add(dueTimeLabel);
        formPanel.add(dueTimeField);
        mainPanel.add(formPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveNewCoupon(idField, nameField, spendMoneyField, offMoneyField,
                             categoryField, dueDateField, dueTimeField)) {
            	allCouponList = couponService.getAllCouponTemplates();
                int selectedIndex = filterComboBox.getSelectedIndex();
                List<Coupon> filteredCoupons = selectedIndex == 0 ? allCouponList : 
                	couponService.getAvailableCouponTemplate();
                updateCouponPanel(filteredCoupons);
                addDialog.dispose();
            }
        });
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);
        addDialog.add(mainPanel, BorderLayout.CENTER);
        addDialog.setVisible(true);
    }
    private boolean saveNewCoupon(JTextField idField, JTextField nameField, JTextField spendMoneyField,
                                 JTextField offMoneyField, JTextField categoryField, JTextField dueDateField,
                                 JTextField dueTimeField) {
        try {
            String couponId = idField.getText().trim();
            String name = nameField.getText().trim();
            String spendMoneyText = spendMoneyField.getText().trim();
            String offMoneyText = offMoneyField.getText().trim();
            String category = categoryField.getText().trim();
            if(category.isEmpty()) { category = null; }
            else {
            	List<String>allCategories = productService.getAllCategories();
            	if(!allCategories.contains(category)) {
            		JOptionPane.showMessageDialog(null, "查无此类别", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            }
            String dueDate = dueDateField.getText().trim();
            String dueTime = dueTimeField.getText().trim();
            if (name.isEmpty() || spendMoneyText.isEmpty() || offMoneyText.isEmpty() ||
            		(dueDate.isEmpty() && !dueTime.isEmpty()) || (!dueDate.isEmpty() && dueTime.isEmpty())) {
                JOptionPane.showMessageDialog(null, "缺少必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            Coupon existingCoupon = couponService.getCouponById(couponId);
            if (existingCoupon != null) {
                JOptionPane.showMessageDialog(null, "优惠券Id已存在", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            double spendMoney, offMoney;
            try {
                spendMoney = Double.parseDouble(spendMoneyText);
                if (spendMoney <= 0) {
                    JOptionPane.showMessageDialog(null, "满减条件必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "满减条件必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            try {
                offMoney = Double.parseDouble(offMoneyText);
                if (offMoney <= 0) {
                    JOptionPane.showMessageDialog(null, "减金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
                if (offMoney >= spendMoney) {
                    JOptionPane.showMessageDialog(null, "减金额必须小于满减条件", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; } 
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "减金额必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            LocalDateTime dueDateTime;
            try {
            	if(dueDate.isEmpty() && dueTime.isEmpty()) { dueDateTime = null; }
            	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        		LocalDate date = LocalDate.parse(dueDate, dateFormatter);
        		LocalTime time = LocalTime.parse(dueTime, timeFormatter);
        		dueDateTime = LocalDateTime.of(date, time);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "日期格式不正确，请使用yyyy-MM-dd格式", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            Coupon newCoupon = new Coupon(couponId, name, spendMoney, offMoney, category, dueDateTime);
            couponService.addCouponTemplate(newCoupon);
            JOptionPane.showMessageDialog(null, "优惠券添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            return true; 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "添加优惠券时发生错误: " + e.getMessage(), 
                                       "错误", JOptionPane.ERROR_MESSAGE);
            return false; }
    }
    private void performCouponSearch(int searchType, String keyword) {
    	if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词", "提示", JOptionPane.INFORMATION_MESSAGE);
            return; }
    	try {
            couponPanel.removeAll();
            List<Coupon> searchResults = new ArrayList<>();
            List<Coupon> Coupons = (filterComboBox.getSelectedIndex() == 0 ? 
            		couponService.getAllCouponTemplates() : 
            			couponService.getAvailableCouponTemplate());
            if (searchType == 0) {
                for (Coupon coupon : Coupons) {
                    if (coupon.getCouponId().contains(keyword)) {
                        searchResults.add(coupon); } }
            } else { 
                for (Coupon coupon : Coupons) {
                    if (coupon.getCategory().contains(keyword)) {
                        searchResults.add(coupon); } }
            }
            for (Coupon coupon : searchResults) {
                couponPanel.add(createCouponItemPanel(coupon));
                couponPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
            if(searchResults.isEmpty()) {
                JLabel noResultsLabel = new JLabel("没有找到相关优惠券");
                noResultsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
                noResultsLabel.setForeground(new Color(120, 120, 120));
                noResultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                couponPanel.add(noResultsLabel);
            }
            couponPanel.add(Box.createVerticalGlue());
            couponPanel.revalidate();
            couponPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "搜索过程中发生错误: " + ex.getMessage(), 
                         "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createCWindow() {
    	JFrame cWindow = new JFrame("管理折扣活动信息");
        cWindow.setSize(480, 500);
        cWindow.setLocationRelativeTo(this);
        cWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Color backgroundColor = new Color(245, 245, 245);
        
        allSaleProList = salePromotionService.getAllPromotions();
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(backgroundColor);
        cWindow.setContentPane(contentPanel);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(backgroundColor);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel filterLabel = new JLabel("筛选:");
        filterLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterComboBoxforC = new JComboBox<>();
        filterComboBoxforC.addItem("全部活动");
        filterComboBoxforC.addItem("未过期活动");
        filterComboBoxforC.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBoxforC);
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        
        saleProPanel = new JPanel();
        saleProPanel.setLayout(new BoxLayout(saleProPanel, BoxLayout.Y_AXIS));
        saleProPanel.setBackground(backgroundColor);
        JScrollPane saleProScrollPane = new JScrollPane(saleProPanel);
        saleProScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        saleProScrollPane.getViewport().setBackground(backgroundColor);
        contentPanel.add(saleProScrollPane, BorderLayout.CENTER);
        
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.setBackground(backgroundColor);
        JComboBox<String> searchTypeComboBox = new JComboBox<>();
        searchTypeComboBox.addItem("按活动Id搜索");
        searchTypeComboBox.addItem("按商品Id搜索");
        searchTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchTypeComboBox.setPreferredSize(new Dimension(100, 25));
        searchPanel.add(searchTypeComboBox, BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setPreferredSize(new Dimension(120, 25));
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchButton.setPreferredSize(new Dimension(60, 25));
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton addSaleProBtn = new JButton("添加折扣活动");
        addSaleProBtn.setFont(new Font("微软雅黑", Font.BOLD, 12));
        addSaleProBtn.setPreferredSize(new Dimension(110, 25));
        buttonPanel.add(addSaleProBtn, BorderLayout.WEST);
        buttonPanel.add(searchPanel, BorderLayout.EAST);
        
        filterComboBoxforC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = filterComboBoxforC.getSelectedIndex();
                List<SalePromotion> filteredSalePros;
                if (selectedIndex == 0) {
                    filteredSalePros = salePromotionService.getAllPromotions();
                    allSaleProList = filteredSalePros;
                } else {
                    filteredSalePros = getAvailableSalePros(); }
                updateSaleProPanel(filteredSalePros);
            }
        });
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSaleProSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSaleProSearch(searchTypeComboBox.getSelectedIndex(), searchField.getText().trim());
            }
        });
        addSaleProBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddSaleProDialog();
            }
        });
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        updateSaleProPanel(allSaleProList);
        
        cWindow.setVisible(true);
    }
    private List<SalePromotion> getAvailableSalePros() {
        List<SalePromotion> availableSalePros = new ArrayList<>();
        List<SalePromotion> allSalePros = salePromotionService.getAllPromotions();
        LocalDateTime now = LocalDateTime.now();
        for (SalePromotion salePro : allSalePros) {
            if (salePro.getDueTime().isAfter(now)) {
                availableSalePros.add(salePro);
            }
        }
        return availableSalePros;
    }
    private void updateSaleProPanel(List<SalePromotion> salePros) {
        saleProPanel.removeAll();
        if (salePros == null || salePros.isEmpty()) {
            JLabel noSaleProsLabel = new JLabel("暂无折扣活动");
            noSaleProsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            noSaleProsLabel.setForeground(Color.GRAY);
            noSaleProsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            saleProPanel.add(noSaleProsLabel);
        } else {
            for (SalePromotion salePro : salePros) {
                saleProPanel.add(createSaleProItemPanel(salePro));
                saleProPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        saleProPanel.add(Box.createVerticalGlue());
        saleProPanel.revalidate();
        saleProPanel.repaint();
    }
    private JPanel createSaleProItemPanel(SalePromotion salePro) {
        Color panelColor = Color.WHITE;
        Color borderColor = new Color(220, 220, 220);
        Color buttonColor = new Color(70, 130, 180);
        Color buttonTextColor = Color.WHITE;
        Color discountColor = new Color(220, 0, 0);
        
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(panelColor);
        JLabel idLabel = new JLabel("活动Id: " + salePro.getPromotionId());
        idLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        idLabel.setForeground(new Color(100, 100, 100));
        String productInfo = (salePro.getProductId() == null ? "全场折扣" : 
        	    "折扣商品： " + salePro.getProductId() + "-" + 
        		productService.getProductById(salePro.getProductId()).getName());
        JLabel productLabel = new JLabel(productInfo);
        productLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        productLabel.setForeground(new Color(100, 100, 100));
        JLabel discountLabel = new JLabel("折扣：" + String.format("%.0f", salePro.getDiscount() * 100) + "%");
        discountLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        discountLabel.setForeground(discountColor);
        String dueDate = salePro.getDueTime().toLocalDate().toString();
        String dueTime = salePro.getDueTime().toLocalTime().toString();
        JLabel dueLabel = new JLabel("到期时间：" + dueDate + " " + dueTime);
        dueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        dueLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(idLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(productLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(discountLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(dueLabel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(panelColor);
        JButton modifyButton = new JButton("修改信息");
        modifyButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        modifyButton.setBackground(buttonColor);
        modifyButton.setForeground(buttonTextColor);
        modifyButton.setFocusPainted(false);
        modifyButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        modifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifySaleProInfo(salePro);
            }
        });
        JButton deleteButton = new JButton("删除活动");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deleteButton.setBackground(new Color(200, 0, 0));
        deleteButton.setForeground(buttonTextColor);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salePromotionService.deletePromotion(salePro.getPromotionId());;
                JOptionPane.showMessageDialog(null, "折扣活动删除成功", "成功", 
                        JOptionPane.INFORMATION_MESSAGE);
                int selectedIndex = filterComboBoxforC.getSelectedIndex();
                List<SalePromotion> filteredSalePros;
                if (selectedIndex == 0) {
                    filteredSalePros = salePromotionService.getAllPromotions();
                    allSaleProList = filteredSalePros;
                } else {
                    filteredSalePros = getAvailableSalePros(); }
                updateSaleProPanel(filteredSalePros);
            }
        });
        buttonPanel.add(modifyButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(deleteButton);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    private void modifySaleProInfo(SalePromotion salePro) {
        JDialog modifyDialog = new JDialog((JFrame) null, "修改折扣活动信息", true);
        modifyDialog.setSize(450, 350);
        modifyDialog.setLocationRelativeTo(null);
        modifyDialog.setLayout(new BorderLayout(10, 10));
        modifyDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel productIdLabel = new JLabel("商品Id:");
        productIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField productIdField = new JTextField(salePro.getProductId() != null ? salePro.getProductId() : "");
        productIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel discountLabel = new JLabel("折扣比例:");
        discountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField discountField = new JTextField(String.valueOf(salePro.getDiscount()));
        discountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        String dueDate = salePro.getDueTime().toLocalDate().toString();
        String dueTime = salePro.getDueTime().toLocalTime().toString();
        JLabel dueDateLabel = new JLabel("到期日期:");
        dueDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueDateField = new JTextField(dueDate);
        dueDateField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueTimeLabel = new JLabel("到期时间:");
        dueTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueTimeField = new JTextField(dueTime);
        dueTimeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(productIdLabel);
        formPanel.add(productIdField);
        formPanel.add(discountLabel);
        formPanel.add(discountField);
        formPanel.add(dueDateLabel);
        formPanel.add(dueDateField);
        formPanel.add(dueTimeLabel);
        formPanel.add(dueTimeField);
        mainPanel.add(formPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveSaleProChanges(salePro, productIdField, discountField, dueDateField, dueTimeField)) {
                modifyDialog.dispose();
                int selectedIndex = filterComboBoxforC.getSelectedIndex();
                List<SalePromotion> filteredSalePros;
                if (selectedIndex == 0) {
                    filteredSalePros = salePromotionService.getAllPromotions();
                    allSaleProList = filteredSalePros;
                } else {
                    filteredSalePros = getAvailableSalePros(); }
                updateSaleProPanel(filteredSalePros);
            }
        });
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);
        modifyDialog.add(mainPanel, BorderLayout.CENTER);
        modifyDialog.setVisible(true);
    }
    private boolean saveSaleProChanges(SalePromotion salePro, JTextField productIdField, 
                        JTextField discountField, JTextField dueDateField, JTextField dueTimeField) {
        try {
            String productId = productIdField.getText().trim();
            if (productId.isEmpty()) {
                productId = null;
            }
            else {
            	if(productService.getProductById(productId) == null) {
            		JOptionPane.showMessageDialog(null, "查无此商品", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
            	}
            	if(salePromotionService.getPromotionsByProductId(productId) != null) {
            		JOptionPane.showMessageDialog(null, "该商品Id已有对应活动", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
            	}
            }
            String discountText = discountField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String dueTime = dueTimeField.getText().trim();
            if (discountText.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
                JOptionPane.showMessageDialog(null, "缺少必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            double discount;
            try {
                discount = Double.parseDouble(discountText);
                if (discount <= 0 || discount > 1) {
                    JOptionPane.showMessageDialog(null, "折扣比例必须在0-1之间", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "折扣比例必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            LocalDateTime dueDateTime;
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalDate date = LocalDate.parse(dueDate, dateFormatter);
                LocalTime time = LocalTime.parse(dueTime, timeFormatter);
                dueDateTime = LocalDateTime.of(date, time);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "日期格式不正确，请使用yyyy-MM-dd和HH:mm:ss格式", 
                                             "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            SalePromotion updatedSalePro = new SalePromotion(productId, discount, dueDateTime, salePro.getPromotionId());
            salePromotionService.updatePromotion(updatedSalePro);
            JOptionPane.showMessageDialog(null, "折扣活动更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "更新折扣活动信息时发生错误: " + e.getMessage(), 
                                       "错误", JOptionPane.ERROR_MESSAGE);
            return false; }
    }
    private void showAddSaleProDialog() {
        JDialog addDialog = new JDialog((JFrame) null, "添加折扣活动", true);
        addDialog.setSize(450, 320);
        addDialog.setLocationRelativeTo(null);
        addDialog.setLayout(new BorderLayout(10, 10));
        addDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel promotionIdLabel = new JLabel("活动Id:");
        promotionIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField promotionIdField = new JTextField();
        promotionIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel productIdLabel = new JLabel("商品Id:");
        productIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField productIdField = new JTextField();
        productIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel discountLabel = new JLabel("折扣比例:");
        discountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField discountField = new JTextField();
        discountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueDateLabel = new JLabel("到期日期:");
        dueDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueDateField = new JTextField();
        dueDateField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel dueTimeLabel = new JLabel("到期时间:");
        dueTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField dueTimeField = new JTextField("23:59:59");
        dueTimeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(promotionIdLabel);
        formPanel.add(promotionIdField);
        formPanel.add(productIdLabel);
        formPanel.add(productIdField);
        formPanel.add(discountLabel);
        formPanel.add(discountField);
        formPanel.add(dueDateLabel);
        formPanel.add(dueDateField);
        formPanel.add(dueTimeLabel);
        formPanel.add(dueTimeField);
        mainPanel.add(formPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            if (saveNewSalePro(promotionIdField, productIdField, discountField, 
                              dueDateField, dueTimeField)) {
            	int selectedIndex = filterComboBoxforC.getSelectedIndex();
                List<SalePromotion> filteredSalePros;
                if (selectedIndex == 0) {
                    filteredSalePros = salePromotionService.getAllPromotions();
                    allSaleProList = filteredSalePros;
                } else {
                    filteredSalePros = getAvailableSalePros(); }
                updateSaleProPanel(filteredSalePros);
                addDialog.dispose();
            }
        });
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);
        addDialog.add(mainPanel, BorderLayout.CENTER);
        addDialog.setVisible(true);
    }
    private boolean saveNewSalePro(JTextField promotionIdField, JTextField productIdField, 
                        JTextField discountField, JTextField dueDateField, JTextField dueTimeField) {
        try {
            String promotionId = promotionIdField.getText().trim();
            String productId = productIdField.getText().trim();
            if (productId.isEmpty()) {
                productId = null;
            }
            else {
            	if(productService.getProductById(productId) == null) {
            		JOptionPane.showMessageDialog(null, "查无此商品", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
            	}
            	if(salePromotionService.getPromotionsByProductId(productId) != null) {
            		JOptionPane.showMessageDialog(null, "该商品Id已有对应活动", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
            	}
            }
            String discountText = discountField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String dueTime = dueTimeField.getText().trim();
            if (promotionId.isEmpty() || discountText.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
                JOptionPane.showMessageDialog(null, "缺少必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            double discount;
            try {
                discount = Double.parseDouble(discountText);
                if (discount <= 0 || discount > 1) {
                    JOptionPane.showMessageDialog(null, "折扣比例必须在0-1之间", "错误", JOptionPane.ERROR_MESSAGE);
                    return false; }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "折扣比例必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            LocalDateTime dueDateTime;
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalDate date = LocalDate.parse(dueDate, dateFormatter);
                LocalTime time = LocalTime.parse(dueTime, timeFormatter);
                dueDateTime = LocalDateTime.of(date, time);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "日期格式不正确，请使用yyyy-MM-dd和HH:mm:ss格式", 
                                             "错误", JOptionPane.ERROR_MESSAGE);
                return false; }
            List<SalePromotion> existingSalePros = salePromotionService.getAllPromotions();
            for (SalePromotion salePro : existingSalePros) {
                if (salePro.getPromotionId().equals(promotionId)) {
                    JOptionPane.showMessageDialog(null, "活动Id已存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            SalePromotion newSalePro = new SalePromotion(productId, discount, dueDateTime, promotionId);
            salePromotionService.addPromotion(newSalePro);
            JOptionPane.showMessageDialog(null, "折扣活动添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "添加折扣活动时发生错误: " + e.getMessage(), 
                                       "错误", JOptionPane.ERROR_MESSAGE);
            return false; }
    }
    private void performSaleProSearch(int searchType, String keyword) {
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请输入搜索关键词", "提示", JOptionPane.INFORMATION_MESSAGE);
            return; }
        try {
            saleProPanel.removeAll();
            List<SalePromotion> searchResults = new ArrayList<>();
            int selectedIndex = filterComboBoxforC.getSelectedIndex();
            List<SalePromotion> salePros;
            if (selectedIndex == 0) {
                salePros = salePromotionService.getAllPromotions();
            } else {
                salePros = getAvailableSalePros(); }
            if (searchType == 0) {
                for (SalePromotion salePro : salePros) {
                    if (salePro.getPromotionId().contains(keyword)) {
                        searchResults.add(salePro);
                    }
                }
            } else {
                for (SalePromotion salePro : salePros) {
                    if (salePro.getProductId() != null && salePro.getProductId().contains(keyword)) {
                        searchResults.add(salePro);
                    }
                }
            }
            for (SalePromotion salePro : searchResults) {
                saleProPanel.add(createSaleProItemPanel(salePro));
                saleProPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
            if (searchResults.isEmpty()) {
                JLabel noResultsLabel = new JLabel("没有找到相关折扣活动");
                noResultsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
                noResultsLabel.setForeground(new Color(120, 120, 120));
                noResultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                saleProPanel.add(noResultsLabel);
            }
            saleProPanel.add(Box.createVerticalGlue());
            saleProPanel.revalidate();
            saleProPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "搜索过程中发生错误: " + ex.getMessage(), 
                         "错误", JOptionPane.ERROR_MESSAGE); }
    }
    
    private void createDWindow() {
    	JFrame dWindow = new JFrame("订单管理");
        dWindow.setSize(800, 600);
        dWindow.setLocationRelativeTo(this);
        dWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Color backgroundColor = new Color(245, 245, 245);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(backgroundColor);
        dWindow.setContentPane(contentPanel);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(backgroundColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel filterLabel = new JLabel("筛选方式:");
        filterLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JComboBox<String> filterComboBox = new JComboBox<>();
        filterComboBox.addItem("所有订单");
        filterComboBox.addItem("按学生Id");
        filterComboBox.addItem("按商品Id");
        filterComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchField.setEnabled(false);
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = filterComboBox.getSelectedIndex();
                searchField.setEnabled(selectedIndex > 0);
                if (selectedIndex == 0) {
                    searchField.setVisible(false);
                    searchButton.setVisible(false);
                }
                else {
                	searchField.setVisible(true);
                    searchButton.setVisible(true);
                }
            }
        });
        searchPanel.add(filterLabel);
        searchPanel.add(filterComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"订单Id", "用户Id", "实付金额", "优惠金额", "创建时间", "期望时间", "配送地址", "支付方式"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        orderTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        orderTable.setRowHeight(25);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(orderTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("订单详情"));
        detailPanel.setBackground(backgroundColor);
        JTextArea orderDetailArea = new JTextArea();
        orderDetailArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        orderDetailArea.setEditable(false);
        orderDetailArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane detailScrollPane = new JScrollPane(orderDetailArea);
        detailScrollPane.setPreferredSize(new Dimension(0, 150));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.5);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        loadAllOrders();
        searchField.setVisible(false);
        searchButton.setVisible(false);
        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filterType = filterComboBox.getSelectedIndex();
                String keyword = searchField.getText().trim();
                if (filterType > 0 && keyword.isEmpty()) {
                    JOptionPane.showMessageDialog(dWindow, "请输入搜索关键词", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                switch (filterType) {
                    case 0:
                        loadAllOrders();
                        break;
                    case 1:
                        loadOrdersByStudentId(keyword);
                        break;
                    case 2:
                        loadOrdersByProductId(keyword);
                        break;
                }
            }
        });
        orderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = orderTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
                        displayOrderDetails(orderDetailArea, orderId);
                    }
                }
            }
        }); 
        dWindow.setVisible(true);
    }
    private void loadAllOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = shopService.getAllOrders();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Order order : orders) {
            Object[] rowData = {
                order.orderId,
                order.userId,
                String.format("¥%.2f", order.finalAmount),
                String.format("¥%.2f", order.Off),
                order.createTime.format(formatter),
                order.expectTime != null ? order.expectTime.format(formatter) : "N/A",
                order.address,
                order.payWay
            };
            tableModel.addRow(rowData);
        }
    }
    private void loadOrdersByStudentId(String studentId) {
    	if(shopService.getShopProfile(studentId) == null) {
        	JOptionPane.showMessageDialog(null, "找不到该学生", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        List<Order> orders = shopService.getOrdersByUserId(studentId);
        if (orders.isEmpty()) {
            JOptionPane.showMessageDialog(null, "该学生未创建过订单", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Order order : orders) {
            Object[] rowData = {
                order.orderId,
                order.userId,
                String.format("¥%.2f", order.finalAmount),
                String.format("¥%.2f", order.Off),
                order.createTime.format(formatter),
                order.expectTime != null ? order.expectTime.format(formatter) : "N/A",
                order.address,
                order.payWay
            };
            tableModel.addRow(rowData);
        }
    }
    private void loadOrdersByProductId(String productId) {
    	if(productService.getProductById(productId) == null) {
        	JOptionPane.showMessageDialog(null, "查无此商品", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        List<Order> allOrders = shopService.getAllOrders();
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : allOrders) {
            for (OrderItem item : order.items) {
                if (item.productId.equals(productId)) {
                    filteredOrders.add(order);
                    break;
                }
            }
        }
        if (filteredOrders.isEmpty()) {
            JOptionPane.showMessageDialog(null, "未找到包含该商品的订单", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Order order : filteredOrders) {
            Object[] rowData = {
                order.orderId,
                order.userId,
                String.format("¥%.2f", order.finalAmount),
                String.format("¥%.2f", order.Off),
                order.createTime.format(formatter),
                order.expectTime != null ? order.expectTime.format(formatter) : "N/A",
                order.address,
                order.payWay
            };
            tableModel.addRow(rowData);
        }
    }
    private void displayOrderDetails(JTextArea detailArea, String orderId) {
        List<Order> allOrders = shopService.getAllOrders();
        Order selectedOrder = null;
        for (Order order : allOrders) {
            if (order.orderId.equals(orderId)) {
                selectedOrder = order;
                break;
            }
        }
        if (selectedOrder == null) {
            detailArea.setText("未找到订单详情");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder details = new StringBuilder();
        details.append("订单Id: ").append(selectedOrder.orderId).append("\n");
        details.append("用户Id: ").append(selectedOrder.userId).append("\n");
        details.append("实付金额: ").append(String.format("¥%.2f", selectedOrder.finalAmount)).append("\n");
        details.append("优惠金额: ").append(String.format("¥%.2f", selectedOrder.Off)).append("\n");
        details.append("创建时间: ").append(selectedOrder.createTime.format(formatter)).append("\n");
        details.append("期望时间: ").append(selectedOrder.expectTime != null ? selectedOrder.expectTime.format(formatter) : "N/A").append("\n");
        details.append("配送地址: ").append(selectedOrder.address).append("\n");
        details.append("支付方式: ").append(selectedOrder.payWay).append("\n\n");
        details.append("订单项:\n");
        for (OrderItem item : selectedOrder.items) {
            Product product = productService.getProductById(item.productId);
            details.append(String.format("  商品: %s (%s) - 数量: %d - 单价: ¥%.2f - 小计: ¥%.2f\n", 
                product.getName(), item.productId, item.quantity, item.price, item.total));
        }
        
        detailArea.setText(details.toString());
    }
}

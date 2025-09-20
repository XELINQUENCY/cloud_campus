package gui.shop;

import entity.User;
import entity.shop.*;
import client.shop.ShopClient;
import client.ApiClientFactory;
import client.ApiException;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ShopView extends JFrame {
	private final ShopClient shopClient;
    private ShopProfile studentUser;
    private JPanel contentPane;
    private JPanel mainMaskPanel;
    private JFrame myWindow;
    private JPanel myMaskPanel;
    private JPanel checkMaskPanel;
    private RoundedButton button1;
    private RoundedButton button2;
    private Map<String, List<Product>> productMap = new HashMap<>();
    private Map<String, Product> productIdMap;
    private JList<String> categoryList; //类别列表
    private JPanel productPanel;
    private ArrayList<Product>shoppingCart = new ArrayList<>();
    private CircleLabel cartCountLabel;
    private JTextField searchField;
    private JLabel titleLabel;
    private JLabel totalLabel2;
    private RoundedButton confirmBuyButton;
    private JPanel cartItemsPanel;
    private Address selectedAddress;
    private JFrame buyWindow;
    private JPanel maskPanel;
    private JLabel addressLabel;
    private JLabel contactLabel;
    private RoundedButton chooseAddrButton;
    private int clickCount = 0;
    private RoundedButton chooseTimeButton;
    private JLabel wayLabel;
    private JLabel timeLabel;
    private JTextField phoneTextField;
    private JButton cButton;
    private List<Coupon>couponList = new ArrayList<>();
    private List<SalePromotion> initialSaleList = new ArrayList<>();
    private ArrayList<SalePromotion>saleList = new ArrayList<>();
    private double couponOff;
    private Coupon selectedCoupon;
    private JLabel label1;
    private double saleOff;
    private JLabel label2;
    private JLabel priceLabel2;
    private JLabel arLabel;
    private JButton mButton;
    private JTextArea remarkTextArea;
    final RoundedButton[] selectedButton = {null};
    private JPanel subMaskPanel;
    private final Runnable onExitCallback;

    void initializeUser(String userId){
    	new SwingWorker<ShopProfile, Void>() {
            @Override
            protected ShopProfile doInBackground() throws Exception {
                ShopProfile profile = shopClient.getMyShopProfile();
                if (profile != null) {
                	profile.myCouponList = (shopClient.getMyCoupons() == null ? 
                			new ArrayList<>() : shopClient.getMyCoupons());
                	profile.historyOrders = (shopClient.getMyOrders() == null ? 
                    		new ArrayList<>() : shopClient.getMyOrders());
                	profile.addressListModel = (shopClient.getMyAddresses() == null ? 
                    		new ArrayList<>() : shopClient.getMyAddresses());
                } else {
                    shopClient.initializeShopProfile();
                    profile = new ShopProfile(userId, 0, 0, "123456");
                }
                return profile;
            }

            @Override
            protected void done() {
                try {
                    studentUser = get();
                    try {
						initializeProducts();
						initializeSaleAndCoupon();
					} catch (ApiException e) {
						e.printStackTrace();
					}
                    contentPane.revalidate();
                    contentPane.repaint();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    private void initializeProducts() throws ApiException {
    	productMap = shopClient.getProductsGroupedByCategory();
        productIdMap = new HashMap<String, Product>();
        for (List<Product> products : productMap.values()) {
            for (Product p : products) {
                productIdMap.put(p.getProductId(), p);
            }
        }
        JLabel categoryLabel = new JLabel("商品类别");
        categoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        categoryLabel.setBounds(110, 120, 100, 200);
        categoryLabel.setOpaque(false);
        contentPane.add(categoryLabel);

        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (String category : productMap.keySet()) {
            listModel.addElement(category);
        }
        categoryList = new JList<String>(listModel) {
            @Override
            protected void paintBorder(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.dispose();
            }
        };
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("宋体", Font.ITALIC, 20));
        categoryList.setFixedCellHeight(35);
        categoryList.setOpaque(false);
        categoryList.setBackground(new Color(0, 0, 0, 0));
        //自定义单元格渲染器，使每个单元格也透明
        categoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(new Color(0, 0, 0, 128));
                c.setForeground(new Color(130, 130, 130));
                if (isSelected)  c.setForeground(Color.BLACK);
                ((JComponent) c).setOpaque(false);
                return c;
            }
            @Override
            protected void paintBorder(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.dispose();
            }
        });
        categoryList.setSelectedIndex(0);
        categoryList.addListSelectionListener(new ListSelectionListener() { //添加类别选择监听器
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) updateProductPanel(categoryList.getSelectedValue());
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setBounds(100, 240, 112, 470);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        contentPane.add(scrollPane);

        //选中类别的商品面板
        productPanel = new JPanel();
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        productPanel.setOpaque(false);
        productPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane productScrollPane = new JScrollPane(productPanel);
        productScrollPane.setBounds(250, 198, 500, 510);
        customizeScrollBars(productScrollPane);
        productScrollPane.setOpaque(false);
        productScrollPane.getViewport().setOpaque(false);
        productScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPane.add(productScrollPane);

        //初始产品面板（第一个）
        updateProductPanel(categoryList.getSelectedValue());

        //搜索商品栏
        JPanel searchPanel = new JPanel();
        searchPanel.setBounds(307, 152, 700, 40);
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setPreferredSize(new Dimension(150, 28));
        searchPanel.add(searchField, BorderLayout.CENTER);
        ImageIcon search = new ImageIcon("./lib/111暑期-vcampus/search.png");
        JButton searchButton = new JButton(search);
        searchButton.setPreferredSize(new Dimension(28, 28));
        searchPanel.add(searchButton, BorderLayout.EAST);
        contentPane.add(searchPanel);

        searchButton.addActionListener(new ActionListener() {  //为搜索按钮添加监听器
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        searchField.addActionListener(new ActionListener() {  //为搜索框添加按键监听器，支持回车键搜索
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }
    private void initializeSaleAndCoupon() throws ApiException {
    	couponList = shopClient.getAllCouponTemplates();
        initialSaleList = shopClient.getAllPromotions();
        if(initialSaleList.isEmpty()) { return; }
        else if (initialSaleList.get(0).getProductId() == null) {  //全场促销
            JPanel allSalePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    int w = 247, h = 220;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gradient = new GradientPaint(w/2, h/2, new Color(255, 105, 97),
                            0, w, new Color(238, 238, 238));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, w, h);
                    g2d.setColor(new Color(255, 255, 255, 50));
                    for (int i = 0; i < 30; i++) {
                        int size = (int) (Math.random() * 10 + 5);
                        int x = (int) (Math.random() * w);
                        int y = (int) (Math.random() * h);
                        g2d.fillOval(x, y, size, size);
                    }
                    g2d.setColor(new Color(255, 236, 131));
                    int[] xPoints = {0, w/4, w/2, w*3/4, w};
                    int[] yPoints = {h/4, h/2, h/4, h/2, h/4};
                    g2d.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawPolyline(xPoints, yPoints, 5);
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
                    g2d.setColor(Color.WHITE);
                    String title = "限时特惠";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (130 - fm.stringWidth(title)) / 2;
                    int y = 20;
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.drawString(title, x + 1, y + 1);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(title, x, y);
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, 60));
                    g2d.setColor(Color.WHITE);
                    String discountStr = (int)(initialSaleList.get(0).getDiscount()*100) + "折";
                    FontMetrics fm_ = g2d.getFontMetrics();
                    int x_ = (w - fm_.stringWidth(discountStr)) / 2;
                    int y_ = h/2;
                    g2d.drawString(discountStr, x_, y_);
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, 13));
                    g2d.setColor(Color.WHITE);
                    String footer1 = "活动时间：即日起至";
                    String footer2 = initialSaleList.get(0).getDueTime().format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
                    FontMetrics fm1 = g2d.getFontMetrics();
                    int x1 = (w - fm1.stringWidth(footer1)) / 2;
                    FontMetrics fm2 = g2d.getFontMetrics();
                    int x2 = (w - fm2.stringWidth(footer2)) / 2;
                    g2d.drawString(footer1, x1, h/4*3);
                    g2d.drawString(footer2, x2, h/6*5);
                }
            };
            allSalePanel.setBounds(750, 200, 247, 220);
            contentPane.add(allSalePanel);
            for(String key : productMap.keySet()) {
                List<Product>products = productMap.get(key);
                for(Product p : products) {
                    saleList.add(new SalePromotion(p.getProductId(),
                            initialSaleList.get(0).getDiscount(),
                            initialSaleList.get(0).getDueTime(), null));
                }
            }
        }
        else {   //特定促销
            for (SalePromotion sale : initialSaleList) { saleList.add(sale); }
        }
        JPanel salePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(3));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                g2d.dispose();
            }
        };
        salePanel.setOpaque(false);
        salePanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        salePanel.setBounds(248, 28, 500, 130);
        JPanel showPanel = new JPanel(new BorderLayout());
        JPanel locatePanel = new JPanel(new BorderLayout());
        locatePanel.setBorder(new EmptyBorder(0, 0, 6, 0));
        locatePanel.setBackground(Color.WHITE);
        JLabel locateLabel1 = new JLabel(new ImageIcon("./lib/111暑期-vcampus/locate.png"));
        JLabel locateLabel2 = new JLabel(" 江苏省南京市xx区xx街道xx大学路xx号 xx大学x园食堂北二楼内部");
        locateLabel2.setFont(new Font("华文黑体", Font.BOLD, 13));
        locatePanel.add(locateLabel1, BorderLayout.WEST); locatePanel.add(locateLabel2, BorderLayout.CENTER);
        showPanel = new JPanel(new BorderLayout());
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBorder(new EmptyBorder(0, 0, 6, 0));
        notePanel.setBackground(Color.WHITE);
        JLabel noteLabel1 = new JLabel(new ImageIcon("./lib/111暑期-vcampus/note.png"));
        JLabel noteLabel2 = new JLabel(" 提供高品质配送服务 配送时间：00:00-23:30");
        noteLabel2.setFont(new Font("华文黑体", Font.BOLD, 13));
        notePanel.add(noteLabel1, BorderLayout.WEST); notePanel.add(noteLabel2, BorderLayout.CENTER);
        JPanel telePanel = new JPanel(new BorderLayout());
        telePanel.setBackground(Color.WHITE);
        JLabel teleLabel1 = new JLabel(new ImageIcon("./lib/111暑期-vcampus/telephone.png"));
        JLabel teleLabel2 = new JLabel(" 135****9520");
        teleLabel2.setFont(new Font("华文黑体", Font.BOLD, 13));
        telePanel.add(teleLabel1, BorderLayout.WEST); telePanel.add(teleLabel2, BorderLayout.CENTER);
        showPanel.add(locatePanel, BorderLayout.NORTH);
        showPanel.add(notePanel, BorderLayout.CENTER);
        showPanel.add(telePanel, BorderLayout.SOUTH);
        salePanel.add(showPanel, BorderLayout.NORTH);
        JPanel activityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        activityPanel.setBackground(Color.WHITE);
        JLabel activityLabel = new JLabel("公告：欢迎你的到来！");
        activityLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        activityPanel.add(activityLabel);
        salePanel.add(activityPanel, BorderLayout.CENTER);
        JPanel buttonContentPanel = new JPanel(new BorderLayout());
        buttonContentPanel.setLayout(new BoxLayout(buttonContentPanel, BoxLayout.X_AXIS));
        buttonContentPanel.setBackground(Color.WHITE);
        RoundedButton couponButton = new RoundedButton("", Color.WHITE, Color.WHITE, Color.WHITE
                , Color.LIGHT_GRAY, 12, 1);
        couponButton.setPreferredSize(new Dimension(400, 25));
        couponButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createAllCouponWindow();
                setGlassPaneVisible(mainMaskPanel, true);
            }});

        boolean isTake1 = false, isTake2 = false, isUsed1 = false, isUsed2 = false;
        String text1 = " | 领券", text2 = " | 领券";
        for(Coupon couponInList : studentUser.myCouponList) {
            if(couponInList.getCouponId().equals(couponList.get(0).getCouponId())) {
                if(couponInList.isUsed()) { isUsed1 = true; }
                isTake1 = true;
            }
            if(couponInList.getCouponId().equals(couponList.get(1).getCouponId())) {
                if(couponInList.isUsed()) { isUsed2 = true; }
                isTake2 = true;
            }
        }
        if(isTake1) {
            if(isUsed1) { text1 = " | 已使用"; }
            else { text1 = " | 已领取"; } }
        if(isTake2) {
            if(isUsed2) { text2 = " | 已使用"; }
            else { text2 = " | 已领取"; } }
        button1 = new RoundedButton(couponList.get(0).getName() + " " +
                (int)couponList.get(0).getSpendMoney() + "减" + (int)couponList.get(0).getOffMoney() + text1,
                new Color(255, 118, 38), new Color(223, 122, 63), new Color(198, 110, 59), Color.WHITE, 10, 0);
        button1.setPreferredSize(new Dimension(170, 18));
        button1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        button1.setForeground(Color.WHITE);
        button1.setEnabled(!isTake1);
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        shopClient.addUserCoupon(couponList.get(0));
                        return null;
                    }
                    @Override
                    protected void done() {
                        try {
                            get();
                            studentUser.myCouponList.add(couponList.get(0));
                            button1.setText(couponList.get(0).getName() + " " + (int)couponList.get(0).getSpendMoney()
                                    + "减" + (int)couponList.get(0).getOffMoney() + " | 已领取");
                            button1.setEnabled(false);
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }});
        button2 = new RoundedButton(couponList.get(1).getName() + " " +
                (int)couponList.get(1).getSpendMoney() + "减" + (int)couponList.get(1).getOffMoney() + text2,
                new Color(255, 118, 38), new Color(223, 122, 63), new Color(198, 110, 59), Color.WHITE, 10, 0);
        button2.setPreferredSize(new Dimension(170, 18));
        button2.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        button2.setForeground(Color.WHITE);
        button2.setEnabled(!isTake2);
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                    	shopClient.addUserCoupon(couponList.get(1));
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            studentUser.myCouponList.add(couponList.get(1));
                            button2.setText(couponList.get(1).getName() + " " + (int)couponList.get(1).getSpendMoney()
                                    + "减" + (int)couponList.get(1).getOffMoney() + " | 已领取");
                            button2.setEnabled(false);
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }});
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(button1); buttonsPanel.add(button2);
        JLabel allLabel = new JLabel("全部优惠 >  ");
        allLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        allLabel.setBackground(Color.WHITE);
        buttonContentPanel.add(buttonsPanel, BorderLayout.WEST);
        buttonContentPanel.add(allLabel, BorderLayout.EAST);
        couponButton.add(buttonContentPanel);
        salePanel.add(couponButton, BorderLayout.SOUTH);
        contentPane.add(salePanel);

    }

    public ShopView(User user, Runnable onExitCallback) {
        this.onExitCallback = onExitCallback;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onExitCallback != null) {
                    onExitCallback.run();
                }
            }
        });
    	this.shopClient = ApiClientFactory.getShopClient();
        setBounds(100, 100, 450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 760);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 760, 12, 12));

        final Image backgroundImage = new ImageIcon("./lib/111暑期-vcampus/shop.jpg").getImage();
        contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图片
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(3));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                g2d.dispose();
            }
        };

        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        setBackground(new Color(0, 0, 0, 0));

        mainMaskPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainMaskPanel.setOpaque(false);
        mainMaskPanel.addMouseListener(new MouseAdapter() {});
        mainMaskPanel.addMouseMotionListener(new MouseMotionAdapter() {});
        mainMaskPanel.addKeyListener(new KeyAdapter() {});
        mainMaskPanel.setFocusable(true);
        mainMaskPanel.setVisible(false);
        setGlassPane(mainMaskPanel);
        JPanel leftUpPanel = new JPanel();
        leftUpPanel.setOpaque(false);
        leftUpPanel.setBackground(new Color(245, 245, 245));
        JLabel leftLabel = new JLabel("你好，" + user.getName());
        leftLabel.setFont(new Font("微软黑体", Font.BOLD, 14));
        leftLabel.setForeground(Color.GRAY);
        leftUpPanel.add(leftLabel);
        leftUpPanel.setBounds(5, 5, 100, 30);
        contentPane.add(leftUpPanel);
        
        //用户
        initializeUser(user.getId());
        
        //购物车按钮
        JPanel cartViewPanel = new JPanel();
        cartViewPanel.setBounds(770, 520, 200, 100);
        cartViewPanel.setOpaque(false);
        RoundedButton cartButton = new RoundedButton("购物车", new Color(101, 255, 209),
                new Color(112, 219, 187), new Color(98, 165, 145), Color.LIGHT_GRAY, 15, 2);
        cartButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        cartButton.setForeground(Color.BLACK);
        cartButton.setPreferredSize(new Dimension(125, 62));
        cartViewPanel.add(cartButton);
        cartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(mainMaskPanel, true);
                createCartWindow();
            }
        });
        cartCountLabel = new CircleLabel("0"); //右上角标识
        cartCountLabel.setBackground(new Color(255, 50, 50));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        cartCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cartCountLabel.setVerticalAlignment(SwingConstants.CENTER);
        cartCountLabel.setPreferredSize(new Dimension(20, 20));
        cartCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        cartCountLabel.setVisible(false);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(125, 62));
        layeredPane.setOpaque(false);
        cartButton.setBounds(0, 0, 125, 62);
        layeredPane.add(cartButton, JLayeredPane.DEFAULT_LAYER);
        cartCountLabel.setBounds(100, 5, 20, 20);
        layeredPane.add(cartCountLabel, JLayeredPane.PALETTE_LAYER);
        cartViewPanel.add(layeredPane);
        contentPane.add(cartViewPanel);

        //确认购买按钮
        JPanel buyConfirmPanel = new JPanel();
        buyConfirmPanel.setBounds(770, 620, 200, 100);
        buyConfirmPanel.setOpaque(false);
        RoundedButton buyConfirmButton = new RoundedButton("确认结算", new Color(111, 222, 255),
                new Color(116, 194, 218), new Color(104, 158, 175), Color.LIGHT_GRAY, 15, 2);
        buyConfirmButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        buyConfirmButton.setPreferredSize(new Dimension(125, 62));
        buyConfirmPanel.add(buyConfirmButton);
        contentPane.add(buyConfirmPanel);
        buyConfirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	boolean isOutOfStock = false;
            	Map<String, CartItem> cartItemMap = new HashMap<>();
                for (Product product : shoppingCart) {
                    String key = product.getProductId();
                    if (cartItemMap.containsKey(key)) {
                        CartItem item = cartItemMap.get(key);
                        item.increaseQuantity();
                    } else { cartItemMap.put(key, new CartItem(product)); }
                }
                String name = "";
                for(CartItem item : cartItemMap.values()) {
                	if(item.getProduct().getStockAmount() - item.getQuantity() <= 0) {
                		name = item.getProduct().getName(); isOutOfStock = true; break; }
                }
                if(isOutOfStock) {
                	JOptionPane.showMessageDialog(null, name + "库存不足 请等待库存投放", "提示", JOptionPane.WARNING_MESSAGE);
                	return;
                }
                else {
                	setGlassPaneVisible(mainMaskPanel, true);
                    createBuyWindow();
                    clickCount = 0;
                }
            }
        });

        //个人中心按钮
        JPanel myPanel = new JPanel();
        myPanel.setBounds(820, 50, 120, 60);
        myPanel.setOpaque(false);
        RoundedButton myButton = new RoundedButton("个人中心", new Color(255, 120, 150),
                new Color(230, 80, 122), new Color(200, 50, 80), Color.LIGHT_GRAY, 15, 2);
        myButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        myButton.setForeground(Color.DARK_GRAY);
        myButton.setPreferredSize(new Dimension(100, 50));
        myPanel.add(myButton);
        contentPane.add(myPanel);
        myButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(mainMaskPanel, true);
                createMyWindow();
            }
        });

        //退出按钮
        JPanel outPanel = new JPanel();
        outPanel.setBounds(820, 120, 120, 60);
        outPanel.setOpaque(false);
        RoundedButton outButton = new RoundedButton("离开商店", new Color(255, 170, 150),
                new Color(230, 140, 122), new Color(200, 100, 80), Color.LIGHT_GRAY, 15, 2);
        outButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        outButton.setForeground(Color.DARK_GRAY);
        outButton.setPreferredSize(new Dimension(100, 50));
        outPanel.add(outButton);
        contentPane.add(outPanel);
        outButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }
    
    //自定义圆形标识类
  	class CircleLabel extends JLabel {
  	    public CircleLabel(String text) {
  	        super(text);
  	        setOpaque(false);
  	    }
  	    @Override
  	    protected void paintComponent(Graphics g) {
  	        Graphics2D g2 = (Graphics2D) g.create();
  	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  	        
  	        // 绘制圆形背景
  	        g2.setColor(getBackground());
  	        g2.fillOval(0, 0, getWidth(), getHeight());
  	        
  	        // 绘制文本
  	        super.paintComponent(g);
  	        g2.dispose();
  	    }
  	}
  	//自定义滚动条
  	private void customizeScrollBars(JScrollPane scrollPane) {
          scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
              @Override
              protected void configureScrollBarColors() {
                  this.thumbColor = new Color(100, 100, 100, 150);
                  this.trackColor = new Color(240, 240, 240);
              }
              @Override
              protected JButton createDecreaseButton(int orientation) {
                  return createInvisibleButton();
              }
              @Override
              protected JButton createIncreaseButton(int orientation) {
                  return createInvisibleButton();
              }
              private JButton createInvisibleButton() {
                  JButton button = new JButton();
                  button.setPreferredSize(new Dimension(0, 0));
                  button.setMinimumSize(new Dimension(0, 0));
                  button.setMaximumSize(new Dimension(0, 0));
                  return button;
              }
              @Override
              protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) { //绘制圆角矩形滑块
                  Graphics2D g2 = (Graphics2D) g.create();
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2.setColor(thumbColor);
                  g2.fill(new RoundRectangle2D.Double(
                      thumbBounds.x + 2, 
                      thumbBounds.y + 2, 
                      thumbBounds.width - 4, 
                      thumbBounds.height - 4, 
                      10, 10));
                  g2.dispose();
              }
          });
          scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
          
          scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
              @Override
              protected void configureScrollBarColors() {
                  this.thumbColor = new Color(100, 100, 100, 150);
                  this.trackColor = new Color(240, 240, 240);
              }
              @Override
              protected JButton createDecreaseButton(int orientation) {
                  return createInvisibleButton();
              }
              @Override
              protected JButton createIncreaseButton(int orientation) {
                  return createInvisibleButton();
              }
              private JButton createInvisibleButton() {
                  JButton button = new JButton();
                  button.setPreferredSize(new Dimension(0, 0));
                  button.setMinimumSize(new Dimension(0, 0));
                  button.setMaximumSize(new Dimension(0, 0));
                  return button;
              }
              @Override
              protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) { //绘制圆角矩形滑块
                  Graphics2D g2 = (Graphics2D) g.create();
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2.setColor(thumbColor);
                  g2.fill(new RoundRectangle2D.Double(
                      thumbBounds.x + 2, 
                      thumbBounds.y + 2, 
                      thumbBounds.width - 4, 
                      thumbBounds.height - 4, 
                      10, 10));
                  g2.dispose();
              }
          });
          scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
  	}
  	//自定义圆角按钮类
  	class RoundedButton extends JButton {
  	    private Color backgroundColor;
  	    private Color hoverColor;
  	    private Color pressedColor;
  	    private Color borderColor;
  	    private int cornerRadius;
  	    private int borderThickness;

  	    public RoundedButton(String text, Color bgColor, Color hover, 
  	    		Color pressed, Color border, int radius, int thickness) {
  	        super(text);
  	        backgroundColor = bgColor;
  	        hoverColor = hover;  //悬停时
  	        pressedColor = pressed;  //按下时
  	        borderColor = border;
  	        cornerRadius = radius;
  	        borderThickness = thickness;
  	        setContentAreaFilled(false);
  	        setFocusPainted(false);
  	        setBorderPainted(false);
  	        setOpaque(false);
  	        setFont(getFont().deriveFont(Font.BOLD));
  	        setMargin(new Insets(thickness, thickness, thickness, thickness));
  	    }
  	    public void setBackgroundColor(Color color) { this.backgroundColor = color; }
  	    public void setHoverColor(Color color) { this.hoverColor = color; }
  	    public void setPressedColor(Color color) { this.pressedColor = color; }
  	    @Override
  	    protected void paintComponent(Graphics g) {
  	        Graphics2D g2 = (Graphics2D) g.create();
  	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  	        if (getModel().isPressed()) {
  	            g2.setColor(pressedColor);
  	        } else if (getModel().isRollover()) {
  	            g2.setColor(hoverColor);
  	        } else {
  	            g2.setColor(backgroundColor);
  	        }
  	        g2.fillRoundRect(borderThickness, borderThickness, getWidth() - borderThickness * 2, 
  	        		getHeight() - borderThickness * 2, cornerRadius, cornerRadius);
  	        if (borderThickness > 0) {
                  g2.setColor(borderColor);
                  g2.setStroke(new BasicStroke(borderThickness));
                  g2.drawRoundRect(borderThickness / 2, borderThickness / 2, getWidth() - borderThickness, 
                      getHeight() - borderThickness, cornerRadius, cornerRadius);
              }
              g2.setColor(getForeground());
              g2.setFont(getFont());
              FontMetrics fm = g2.getFontMetrics();
              Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
              int textX = (getWidth() - stringBounds.width) / 2;
              int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
              g2.drawString(getText(), textX, textY);
              g2.dispose();
  	    }
  	}


    //创建个人中心窗口
    private void createMyWindow() {
        myWindow = new JFrame();
        myWindow.setSize(320, 410);
        myWindow.setLocationRelativeTo(this);
        myWindow.setUndecorated(true);
        myWindow.setShape(new RoundRectangle2D.Double(0, 0, 320, 410, 20, 20));
        myWindow.setVisible(true);

        myMaskPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, myWindow.getWidth(), myWindow.getHeight());
            }
        };
        myMaskPanel.setOpaque(false);
        myMaskPanel.addMouseListener(new MouseAdapter() {});
        myMaskPanel.addMouseMotionListener(new MouseMotionAdapter() {});
        myMaskPanel.addKeyListener(new KeyAdapter() {});
        myMaskPanel.setFocusable(true);
        myWindow.setGlassPane(myMaskPanel);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(245, 247, 250));
        myWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("个人中心", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
                myMaskPanel = null;
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setOpaque(false);
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.setOpaque(false);
        idPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel idLabel = new JLabel("用户Id: " + studentUser.userId);
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        idLabel.setForeground(new Color(80, 80, 80));
        idPanel.add(idLabel);
        mainContentPanel.add(idPanel);
        JPanel balancePointsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        balancePointsPanel.setOpaque(false);
        balancePointsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel balanceLabel = new JLabel("商店余额:");
        balanceLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JLabel balanceValue = new JLabel("¥" + String.format("%.2f", studentUser.getBalanceShop()));
        balanceValue.setFont(new Font("微软雅黑", Font.BOLD, 15));
        balanceValue.setForeground(new Color(0, 150, 0));
        JLabel pointsLabel = new JLabel("积分（功能开发中）:");
        pointsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JLabel pointsValue = new JLabel(studentUser.getPoints() + " 分");
        pointsValue.setFont(new Font("微软雅黑", Font.BOLD, 15));
        pointsValue.setForeground(new Color(200, 0, 0));
        balancePointsPanel.add(balanceLabel);
        balancePointsPanel.add(balanceValue);
        balancePointsPanel.add(pointsLabel);
        balancePointsPanel.add(pointsValue);
        mainContentPanel.add(balancePointsPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(5, 2, 10, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        RoundedButton couponCenterButton = new RoundedButton("领券中心", new Color(255, 178, 102),
                new Color(255, 153, 51), new Color(255, 128, 0), new Color(0, 0, 0), 10, 0);
        couponCenterButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        couponCenterButton.setForeground(Color.WHITE);
        couponCenterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createAllCouponWindow();
                setGlassPaneVisible(myWindow, myMaskPanel, true);
            }
        });
        RoundedButton myCouponsButton = new RoundedButton("我的优惠券", new Color(255, 178, 102),
                new Color(255, 153, 51), new Color(255, 128, 0), new Color(0, 0, 0), 10, 0);
        myCouponsButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myCouponsButton.setForeground(Color.WHITE);
        myCouponsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createCouponWindow(myWindow);
            }
        });
        RoundedButton rechargeButton = new RoundedButton("储值中心", new Color(76, 175, 80),
                new Color(69, 160, 73), new Color(56, 142, 60), new Color(0, 0, 0), 10, 0);
        rechargeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        rechargeButton.setForeground(Color.WHITE);
        rechargeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	ShopBankRechargeDialog rechargeDialog = new ShopBankRechargeDialog(null, 
            	        new ShopBankRechargeDialog.RechargeCallback() {
            	            @Override
            	            public void onRechargeSuccess(BigDecimal amount, BigDecimal bonus) {
            	            	studentUser.setBalanceShop(studentUser.getBalanceShop() + 
            	            			(amount.add(bonus)).doubleValue());
            	            	new SwingWorker<Void, Void>() {
            	                    @Override
            	                    protected Void doInBackground() throws Exception {
            	                    	shopClient.updateShopProfile(studentUser);
            	                        return null;
            	                    }
            	                    @Override
            	                    protected void done() {
            	                        try {
            	                            get();
            	                            balanceValue.setText("¥" + String.format("%.2f", studentUser.getBalanceShop()));
                                            JOptionPane.showMessageDialog(myWindow,
                                              "充值成功！当前余额: ¥" + String.format("%.2f", studentUser.getBalanceShop()),
                                               "提示", JOptionPane.INFORMATION_MESSAGE);
            	                        } catch (InterruptedException | ExecutionException ex) {
            	                            ex.printStackTrace();
            	                        }
            	                    }
            	                }.execute();
            	            	
                                
            	            }
            	            
            	            @Override
            	            public void onRechargeFailure(String errorMessage) {
            	                // 充值失败的处理
            	            	JOptionPane.showMessageDialog(myWindow,
                                        "充值失败！" + String.format("%.2f", studentUser.getBalanceShop()),
                                         "提示", JOptionPane.INFORMATION_MESSAGE);
            	            }
            	        });
            	    rechargeDialog.setVisible(true);
                
            }
        });
        RoundedButton orderButton = new RoundedButton("我的订单", new Color(76, 175, 80),
                new Color(69, 160, 73), new Color(56, 142, 60), new Color(0, 0, 0), 10, 0);
        orderButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        orderButton.setForeground(Color.WHITE);
        orderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createOrderCheckWindow();
                setGlassPaneVisible(myWindow, myMaskPanel, true);
            }
        });
        RoundedButton modifyPwButton = new RoundedButton("修改密码", new Color(76, 175, 80),
                new Color(69, 160, 73), new Color(56, 142, 60), new Color(0, 0, 0), 10, 0);
        modifyPwButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        modifyPwButton.setForeground(Color.WHITE);
        modifyPwButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPasswordField passwordField = new JPasswordField();
                int result = JOptionPane.showConfirmDialog(
                        myWindow,
                        passwordField,
                        "修改密码",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                	char[] newPassword = passwordField.getPassword();
                    studentUser.setPasswordShop(String.valueOf(newPassword));
                	new SwingWorker<Void, Void>() {
	                    @Override
	                    protected Void doInBackground() throws Exception {
	                    	shopClient.updateShopProfile(studentUser);
	                        return null;
	                    }

	                    @Override
	                    protected void done() {
	                        try {
	                            get();
	                            JOptionPane.showMessageDialog(null, "密码已更新！");
	                            java.util.Arrays.fill(newPassword, '0');
	                        } catch (InterruptedException | ExecutionException ex) {
	                            ex.printStackTrace();
	                        }
	                    }
	                }.execute();
                }
            }
        });
        buttonsPanel.add(couponCenterButton);
        buttonsPanel.add(myCouponsButton);
        buttonsPanel.add(rechargeButton);
        buttonsPanel.add(orderButton);
        buttonsPanel.add(modifyPwButton);
        mainContentPanel.add(buttonsPanel);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        customizeScrollBars(scrollPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    //创建历史订单查看窗口
    private void createOrderCheckWindow() {
        final JFrame orderCheckWindow = new JFrame();
        orderCheckWindow.setSize(500, 500);
        orderCheckWindow.setLocationRelativeTo(myWindow);
        orderCheckWindow.setUndecorated(true);
        orderCheckWindow.setShape(new RoundRectangle2D.Double(0, 0, 500, 500, 20, 20));
        orderCheckWindow.setVisible(true);

        checkMaskPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, orderCheckWindow.getWidth(), orderCheckWindow.getHeight());
            }
        };
        checkMaskPanel.setOpaque(false);
        checkMaskPanel.addMouseListener(new MouseAdapter() {});
        checkMaskPanel.addMouseMotionListener(new MouseMotionAdapter() {});
        checkMaskPanel.addKeyListener(new KeyAdapter() {});
        checkMaskPanel.setFocusable(true);
        orderCheckWindow.setGlassPane(checkMaskPanel);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(245, 245, 245));
        orderCheckWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("我的订单", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                orderCheckWindow.dispose();
                setGlassPaneVisible(myWindow, myMaskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);
        JPanel orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setOpaque(false);

        if (studentUser.historyOrders.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无订单记录", JLabel.CENTER);
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setBorder(new EmptyBorder(50, 0, 0, 0));
            orderListPanel.add(emptyLabel);
        } else {
            for (Order order : studentUser.historyOrders) {
                orderListPanel.add(createOrderSummaryPanel(order, orderCheckWindow));
                orderListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        JScrollPane scrollPane = new JScrollPane(orderListPanel);
        customizeScrollBars(scrollPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }
    //创建订单摘要面板
    private JPanel createOrderSummaryPanel(final Order order, final JFrame parentWindow) {
        final JPanel panel = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setMaximumSize(new Dimension(485, 125));
        panel.setOpaque(false);

        int totalQuantity = 0;
        for (OrderItem item : order.items) {
            totalQuantity += item.quantity;
        }

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        imagePanel.setOpaque(false);
        int displayCount = Math.min(3, order.items.size());
        for (int i = 0; i < displayCount; i++) {
            OrderItem item = order.items.get(i);
            Product product = productIdMap.get(item.productId);
            if (product != null) {
                JPanel bothPanel = new JPanel();
                bothPanel.setBackground(Color.WHITE);
                bothPanel.setLayout(new BoxLayout(bothPanel, BoxLayout.Y_AXIS));
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return shopClient.getProductById(item.productId).getImagePath();
                    }
                    @Override
                    protected void done() {
                        try {

                            ImageIcon originalIcon = new ImageIcon(get());
                            Image scaledImage = originalIcon.getImage().getScaledInstance(82, 82, Image.SCALE_SMOOTH);
                            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                            imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                            JLabel nameLabel = new JLabel(product.getName());
                            nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                            bothPanel.add(imageLabel);
                            bothPanel.add(Box.createRigidArea(new Dimension(0, 2)));
                            bothPanel.add(nameLabel);
                            imagePanel.add(bothPanel);
                            if (order.items.size() > 3) {
                                JLabel moreLabel = new JLabel("+" + (order.items.size() - 3));
                                moreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                                moreLabel.setForeground(new Color(150, 150, 150));
                                moreLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
                                imagePanel.add(moreLabel);
                            }
                            panel.revalidate();
                            panel.repaint();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            }
        }
       
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel quantityLabel = new JLabel("共" + totalQuantity + "件商品  ");
        quantityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        quantityLabel.setForeground(new Color(100, 100, 100));
        JLabel priceLabel = new JLabel("总金额: ¥" + String.format("%.2f", order.finalAmount) + "  ");
        priceLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        priceLabel.setForeground(new Color(255, 100, 0));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(quantityLabel);
        panel.add(imagePanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.EAST);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setGlassPaneVisible(parentWindow, checkMaskPanel, true);
                showOrderDetailWindow(order, parentWindow);  }
            @Override
            public void mouseEntered(MouseEvent e) { panel.setCursor(new Cursor(Cursor.HAND_CURSOR)); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); }
        });
        return panel;
    }
    //显示订单详情窗口
    private void showOrderDetailWindow(Order order, final JFrame parentWindow) {
        final JFrame detailWindow = new JFrame();
        detailWindow.setSize(400, 450);
        detailWindow.setLocationRelativeTo(parentWindow);
        detailWindow.setUndecorated(true);
        detailWindow.setShape(new RoundRectangle2D.Double(0, 0, 500, 450, 20, 20));
        detailWindow.setVisible(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(245, 245, 245));
        detailWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("订单详情", JLabel.LEFT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                detailWindow.dispose();
                setGlassPaneVisible(parentWindow, checkMaskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setOpaque(false);
        detailPanel.add(createDetailItem("订单号", order.orderId));
        detailPanel.add(createDetailItem("下单时间",
                order.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        detailPanel.add(createDetailItem("预计送达",
                order.expectTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        detailPanel.add(createDetailItem("配送地址", order.address));
        detailPanel.add(createDetailItem("支付方式", order.payWay));
        detailPanel.add(createDetailItem("实付金额", "¥" + String.format("%.2f", order.finalAmount)));
        detailPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel productsLabel = new JLabel("商品列表");
        productsLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        productsLabel.setForeground(new Color(70, 70, 70));
        productsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(productsLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        for (OrderItem item : order.items) {
            Product product = productIdMap.get(item.productId);
            if (product != null) {
                detailPanel.add(createProductDetailItem(item));
                detailPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        JScrollPane scrollPane = new JScrollPane(detailPanel);
        customizeScrollBars(scrollPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }
    //创建订单详情项
    private JPanel createDetailItem(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(390, 30));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setPreferredSize(new Dimension(100, 20));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(120, 120, 120));
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }
    //创建商品详情项
    private JPanel createProductDetailItem(OrderItem item) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(390, 80));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        try {
            ImageIcon originalIcon = new ImageIcon(shopClient.
                    getProductById(item.productId).getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setPreferredSize(new Dimension(80, 80));
            panel.add(imageLabel, BorderLayout.WEST);
        } catch (Exception e) {
            //图片加载失败，使用文本替代
            JLabel noImageLabel = new JLabel("无图片");
            noImageLabel.setPreferredSize(new Dimension(50, 50));
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            noImageLabel.setVerticalAlignment(JLabel.CENTER);
            noImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            panel.add(noImageLabel, BorderLayout.WEST);
            panel.revalidate();
            panel.repaint();
        }
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return shopClient.getProductById(item.productId).getName();
            }
            @Override
            protected void done() {
                try {
                	JLabel nameLabel = new JLabel(get());
                    nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                    nameLabel.setForeground(new Color(80, 80, 80));
                    infoPanel.add(nameLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    panel.add(infoPanel, BorderLayout.CENTER);
                    panel.revalidate();
                    panel.repaint();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        JLabel priceLabel = new JLabel("单价¥" + String.format("%.2f", item.price));
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(100, 100, 100));
        JLabel quantityLabel = new JLabel("×" + item.quantity);
        quantityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        quantityLabel.setForeground(new Color(100, 100, 100));
        JLabel totalLabel = new JLabel("小计¥" + String.format("%.2f", item.total));
        totalLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        totalLabel.setForeground(new Color(100, 100, 100));
        rightPanel.add(priceLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(quantityLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(totalLabel);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    //创建所有优惠券窗口
    private void createAllCouponWindow() {
        final JFrame allCouponWindow = new JFrame();
        allCouponWindow.setSize(400, 400);
        allCouponWindow.setLocationRelativeTo(this);
        allCouponWindow.setUndecorated(true);
        allCouponWindow.setShape(new RoundRectangle2D.Double(0, 0, 400, 400, 20, 20));
        allCouponWindow.setVisible(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(245, 245, 245));
        allCouponWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("商店优惠券", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                allCouponWindow.dispose();
                if(myMaskPanel != null) { setGlassPaneVisible(myWindow, myMaskPanel, false); }
                else { setGlassPaneVisible(mainMaskPanel, false); }
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel couponListPanel = new JPanel();
        couponListPanel.setLayout(new BoxLayout(couponListPanel, BoxLayout.Y_AXIS));
        couponListPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(couponListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        customizeScrollBars(scrollPane);
        for (Coupon coupon : couponList) {
            JPanel couponItemPanel = createCouponItemPanel(coupon, allCouponWindow);
            couponListPanel.add(couponItemPanel);
            couponListPanel.add(Box.createRigidArea(new Dimension(0, 7)));
        }
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("优惠券领取后可在结算时使用", JLabel.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(infoLabel, BorderLayout.SOUTH);
    }

    //创建所有优惠券窗口的（每一个）优惠券面板
    private JPanel createCouponItemPanel(final Coupon coupon, JFrame parentWindow) {
        final JPanel couponPanel = new JPanel(new BorderLayout(10, 0));
        couponPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        couponPanel.setBackground(Color.WHITE);
        couponPanel.setMaximumSize(new Dimension(450, 100));

        boolean isClaimed = false, isUsed = false;
        for(Coupon couponInList : studentUser.myCouponList) {
            if(couponInList.getCouponId().equals(coupon.getCouponId())) {
                if(couponInList.isUsed()) { isUsed = true; }
                isClaimed = true; break;
            }
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(coupon.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        nameLabel.setForeground(new Color(60, 60, 60));
        JLabel descriptionLabel = new JLabel(coupon.getCategory() == null ? "在线支付专享" :
                coupon.getCategory() + "类专享");
        descriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descriptionLabel.setForeground(new Color(150, 150, 150));
        JLabel discountLabel = new JLabel("满" + (int)coupon.getSpendMoney() + "减" + (int)coupon.getOffMoney());
        discountLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        discountLabel.setForeground(new Color(255, 118, 38));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        JLabel expiryLabel = new JLabel("有效期至: " + sdf.format(java.sql.Timestamp.valueOf(coupon.getDueTime())));
        expiryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        expiryLabel.setForeground(new Color(150, 150, 150));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(descriptionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(discountLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(expiryLabel);

        final RoundedButton claimButton;
        if (!isClaimed) {
            claimButton = new RoundedButton("领取", new Color(255, 118, 38), new Color(223, 122, 63),
                    new Color(198, 110, 59), new Color(255, 118, 38), 8, 1);
            claimButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                        	shopClient.addUserCoupon(coupon);
							return null;
                        }

                        @Override
                        protected void done() {
                            try {
                            	get();
                            	studentUser.myCouponList.add(coupon);
                            	claimButton.setText("已领取");
                                claimButton.setEnabled(false);
                                claimButton.setBackgroundColor(new Color(200, 200, 200));
                                claimButton.setHoverColor(new Color(200, 200, 200));
                                claimButton.setPressedColor(new Color(180, 180, 180));
                                if(coupon.getCouponId().equals(couponList.get(0).getCouponId())) {
                                    button1.setText(couponList.get(0).getName() + " " + (int)couponList.get(0).getSpendMoney()
                                            + "减" + (int)couponList.get(0).getOffMoney() + " | 已领取");
                                    button1.setEnabled(false);
                                }
                                else if(coupon.getCouponId().equals(couponList.get(1).getCouponId())){
                                    button2.setText(couponList.get(1).getName() + " " + (int)couponList.get(1).getSpendMoney()
                                            + "减" + (int)couponList.get(1).getOffMoney() + " | 已领取");
                                    button2.setEnabled(false);
                                }
                                couponPanel.revalidate();
                                couponPanel.repaint();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute();
                }
            });
        } else {
            if(!isUsed) {
                claimButton = new RoundedButton("已领取", new Color(200, 200, 200), new Color(200, 200, 200),
                        new Color(180, 180, 180), new Color(180, 180, 180), 8, 1);
                claimButton.setEnabled(false);
            }
            else {
                claimButton = new RoundedButton("已核销", new Color(200, 200, 200), new Color(200, 200, 200),
                        new Color(180, 180, 180), new Color(180, 180, 180), 8, 1);
                claimButton.setEnabled(false);
                if(coupon.getCouponId().equals(couponList.get(0).getCouponId())) {
                    button1.setText(couponList.get(0).getName() + " " + (int)couponList.get(0).getSpendMoney()
                            + "减" + (int)couponList.get(0).getOffMoney() + " | 已使用");
                    button1.setEnabled(false);
                }
                else if(coupon.getCouponId().equals(couponList.get(1).getCouponId())){
                    button2.setText(couponList.get(1).getName() + " " + (int)couponList.get(1).getSpendMoney()
                            + "减" + (int)couponList.get(1).getOffMoney() + " | 已使用");
                    button2.setEnabled(false);
                }
            }
        }
        claimButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        claimButton.setForeground(Color.WHITE);
        claimButton.setPreferredSize(new Dimension(80, 30));

        couponPanel.add(infoPanel, BorderLayout.CENTER);
        couponPanel.add(claimButton, BorderLayout.EAST);

        return couponPanel;
    }

    //获取折扣商品的信息和价格
    private SalePromotion getProductSale(String productId) {
        LocalDateTime now = LocalDateTime.now();
        for (SalePromotion sale : saleList) {
            if (sale.getProductId().equals(productId) &&
                    (sale.getDueTime() == null || now.isBefore(sale.getDueTime()))) { return sale; }
        }
        return null;
    }
    private double getDiscountedPrice(Product product) {
        SalePromotion sale = getProductSale(product.getProductId());
        if (sale != null && product.getStockAmount() != 0)
        { return product.getPrice() * sale.getDiscount(); }
        return product.getPrice();
    }
    //获取购物车总价（打折商品第一件打折价，其余原价）和它的重载
    private double getCartTotal(ArrayList<Product>products) {
        double total = 0;
        Map<String, CartItem> cartItemMap = new HashMap<String, CartItem>();
        for (Product product : products) {
            String key = product.getProductId();
            if (cartItemMap.containsKey(key)) {
                CartItem item = cartItemMap.get(key);
                item.increaseQuantity();
            } else { cartItemMap.put(key, new CartItem(product)); }
        }
        for(String key : cartItemMap.keySet()) {
            CartItem item = cartItemMap.get(key);
            total += (getDiscountedPrice(item.getProduct()) +
                    item.getProduct().getPrice() * (item.getQuantity() - 1));
        }
        return total;
    }
    private double getCartTotal(String category) {
        ArrayList<Product>sameCategoryCartList = new ArrayList<>();
        for (Product product : shoppingCart) {
            if(category.equals(product.getCategory())) { sameCategoryCartList.add(product); }
        }
        return getCartTotal(sameCategoryCartList);
    }

    //选择类别时更新商品面板
    private void updateProductPanel(String category) {
        productPanel.removeAll();

        if (category == null) return;
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setOpaque(rootPaneCheckingEnabled);
        categoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        productPanel.add(categoryLabel);

        List<Product> products = productMap.get(category);
        if (products != null && !products.isEmpty()) {
            List<Product> sortedProducts = products.stream()
                    .sorted(Comparator.comparingInt(p -> p.getStockAmount() == 0 ? 1 : 0))
                    .toList();
            for (Product product : sortedProducts) {
                productPanel.add(createProductItemPanel(product));
                productPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        } else {
            JLabel noProductsLabel = new JLabel("该类别暂无商品");
            noProductsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            noProductsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            productPanel.add(noProductsLabel);
        }
        productPanel.revalidate();
        productPanel.repaint();
    }

    //创建选中类别的（每一个）商品面板
    private JPanel createProductItemPanel(final Product product) {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 210));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                if (product.getStockAmount() == 0) {
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                }
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.dispose();
            }
        };
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(650, 120));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setOpaque(false);

        //商品图片
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            if (product.getStockAmount() == 0) {
                scaledImage = GrayFilter.createDisabledImage(scaledImage); }
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("图片加载失败");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            imageLabel.setPreferredSize(new Dimension(100, 100));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        imageLabel.setPreferredSize(new Dimension(100, 100));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.WEST);

        //商品信息
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(new Color(80, 80, 80));

        JLabel descLabel = new JLabel(product.getDescription());
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));

        SalePromotion sale = getProductSale(product.getProductId());
        double discountedPrice = getDiscountedPrice(product);
        JLabel priceLabel = new JLabel("¥" + String.format("%.2f", discountedPrice));
        priceLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        priceLabel.setForeground(product.getStockAmount() == 0 ?
                new Color(100, 100, 100) : new Color(255, 60, 0));

        JLabel soldLabel = new JLabel("月售: " +  (product.getSoldAmount() <= 100 ?
                product.getSoldAmount() : product.getSoldAmount()/10  + "0+"));
        soldLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        soldLabel.setForeground(new Color(150, 150, 150));
        soldLabel.setVisible(product.getStockAmount() != 0);

        //购买按钮
        final RoundedButton buyButton = new RoundedButton("+", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.ORANGE, 15, 1);
        buyButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        buyButton.setForeground(Color.BLACK);
        buyButton.setPreferredSize(new Dimension(30, 25));
        final RoundedButton choiceButton = new RoundedButton("选规格", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.ORANGE, 15, 1);
        choiceButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        choiceButton.setForeground(Color.BLACK);
        choiceButton.setPreferredSize(new Dimension(60, 25));
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        if (product.getStockAmount() == 0) {
            JLabel soldOutLabel = new JLabel("已售罄");
            soldOutLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            soldOutLabel.setForeground(Color.GRAY);
            buttonPanel.add(soldOutLabel);
        } else {
            if(product.getChoice() == null) { buttonPanel.add(buyButton); }
            else { buttonPanel.add(choiceButton); }
        }

        //减按钮
        final RoundedButton backButton = new RoundedButton("-", Color.LIGHT_GRAY,
                new Color(180, 180, 180), new Color(150, 150, 150), Color.BLACK, 15, 1);
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        backButton.setForeground(Color.BLACK);
        backButton.setPreferredSize(new Dimension(30, 25));
        if(product.getChoice() == null) buttonPanel.add(backButton);
        backButton.setVisible(false);
        int count = 0;
        for (Product p : shoppingCart) {
            if (p.getName().equals(product.getName())) count++;
        }
        if (count > 0) {
            buyButton.setText(String.valueOf(count));
            buyButton.setBackground(new Color(255, 200, 0));
            backButton.setVisible(true);

        } else {
            buyButton.setText("+");
            buyButton.setBackground(Color.YELLOW);
            backButton.setVisible(false);
        }

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (sale != null && product.getStockAmount() != 0) {
            Box priceBox = Box.createHorizontalBox();
            priceBox.setOpaque(false);
            JLabel smallLabel = new JLabel("第一件");
            smallLabel.setFont(new Font("微软雅黑", Font.BOLD, 10));
            smallLabel.setForeground(new Color(255, 60, 0));
            priceBox.add(smallLabel);
            priceBox.add(priceLabel);
            priceBox.add(Box.createHorizontalStrut(5));
            JLabel originalPriceLabel = new JLabel("<html><strike>￥" +
                    String.format("%.2f", product.getPrice()) + "</strike></html>");
            originalPriceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            originalPriceLabel.setForeground(new Color(150, 150, 150));
            priceBox.add(originalPriceLabel);
            priceBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(priceBox);
        } else { infoPanel.add(priceLabel); }
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(soldLabel);
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.add(infoPanel, BorderLayout.CENTER);

        buyButton.addActionListener(new ActionListener() {  //添加按钮监听器
            public void actionPerformed(ActionEvent e) {
                shoppingCart.add(product);
                int count = 0;
                for (Product p : shoppingCart) {
                    if (p.getName().equals(product.getName())) count++;
                }
                updateCartCount();
                if (count > 0) {
                    buyButton.setText(String.valueOf(count));
                    buyButton.setBackground(new Color(255, 200, 0));
                    backButton.setVisible(true);

                } else {
                    buyButton.setText("+");
                    buyButton.setBackground(Color.YELLOW);
                }
            }
        });
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < shoppingCart.size(); i++) {
                    if (shoppingCart.get(i).getName().equals(product.getName())) {
                        shoppingCart.remove(i);
                        break;
                    }
                }
                int count = 0;
                for (Product p : shoppingCart) {
                    if (p.getName().equals(product.getName())) count++;
                }
                updateCartCount();
                if(count == 0) {
                    buyButton.setText("+");
                    buyButton.setBackground(Color.YELLOW);
                    backButton.setVisible(false);
                }
                else {
                    buyButton.setText(String.valueOf(count));
                }
            }

        });
        choiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(mainMaskPanel, true);
                createChoiceWindow(product);
            }
        });

        return panel;
    }

    //创建特定商品选规格的窗口
    private void createChoiceWindow(final Product product) {
        final JFrame choiceWindow = new JFrame();
        choiceWindow.setSize(300, 250);
        choiceWindow.setLocationRelativeTo(this);
        choiceWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        choiceWindow.setUndecorated(true);
        choiceWindow.setShape(new RoundRectangle2D.Double(0, 0, 300, 250, 10, 10));
        choiceWindow.setVisible(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);
        choiceWindow.setContentPane(contentPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.setMaximumSize(new Dimension(20, 20));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choiceWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
            }
        });
        JLabel titleLabel = new JLabel(product.getName() + "    ", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBackground(Color.WHITE);
        topPanel.add(closeButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        JLabel descLabel = new JLabel("规格：" + product.getSpecification());
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        descLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        descLabel.setBackground(Color.WHITE);
        JPanel choicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        choicePanel.setBackground(Color.WHITE);
        choicePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        final RoundedButton[] selectedButton = {null};
        String[] choices = product.getChoice().split("\\|");
        for (String option : choices) {
            final RoundedButton choiceButton = new RoundedButton(option, Color.WHITE, Color.LIGHT_GRAY,
                    Color.GRAY, Color.GRAY, 15, 1);
            choiceButton.setPreferredSize(new Dimension(65, 30));
            choiceButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
            choiceButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (selectedButton[0] != null) {
                        selectedButton[0].setBackgroundColor(Color.WHITE);
                        selectedButton[0].setHoverColor(Color.LIGHT_GRAY);
                        selectedButton[0].setPressedColor(Color.GRAY);
                        selectedButton[0].setForeground(Color.BLACK);
                    }
                    choiceButton.setBackgroundColor(new Color(255, 245, 180));
                    choiceButton.setHoverColor(new Color(255, 235, 160));
                    choiceButton.setPressedColor(new Color(255, 225, 140));
                    choiceButton.setForeground(new Color(200, 159, 31));
                    choiceButton.repaint();
                    selectedButton[0] = choiceButton;
                    product.setSpecialSpecification(choiceButton.getText());
                }
            });
            choicePanel.add(choiceButton);
        }
        centerPanel.add(descLabel, BorderLayout.NORTH);
        centerPanel.add(choicePanel, BorderLayout.CENTER);
        centerPanel.add(createSeparator(), BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        JLabel moneyLabel = new JLabel("￥" + String.format("%.2f", getDiscountedPrice(product)));
        if (getProductSale(product.getProductId()) != null) {
            JPanel pricePanel = new JPanel();
            pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
            pricePanel.setOpaque(false);
            pricePanel.add(moneyLabel);
            JLabel originalPriceLabel = new JLabel("<html><strike>  ￥" +
                    String.format("%.2f", product.getPrice()) + "</strike></html>");
            originalPriceLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
            originalPriceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            originalPriceLabel.setForeground(new Color(150, 150, 150));
            pricePanel.add(originalPriceLabel);
            bottomPanel.add(pricePanel, BorderLayout.WEST);
        } else {
            bottomPanel.add(moneyLabel, BorderLayout.WEST);
        }
        moneyLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        moneyLabel.setForeground(new Color(255, 60, 0));
        moneyLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        final RoundedButton addButton = new RoundedButton("+", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.ORANGE, 10, 2);
        addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        addButton.setPreferredSize(new Dimension(25, 25));
        final RoundedButton minButton = new RoundedButton("-", Color.LIGHT_GRAY,
                new Color(180, 180, 180), new Color(150, 150, 150), Color.BLACK, 15, 1);
        minButton.setPreferredSize(new Dimension(25, 25));
        minButton.setVisible(false);
        final int[] count = {0};
        addButton.addActionListener(new ActionListener() {  //添加按钮监听器
            public void actionPerformed(ActionEvent e) {
                if (selectedButton[0] == null) {
                    JOptionPane.showMessageDialog(choiceWindow,
                            "请先选择商品规格", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    Product productWithSpec = product.clone();
                    productWithSpec.setSpecialSpecification(selectedButton[0].getText());
                    shoppingCart.add(productWithSpec);
                    count[0]++;
                    updateCartCount();
                    if (count[0] > 0) {
                        addButton.setText(String.valueOf(count[0]));
                        addButton.setBackground(new Color(255, 200, 0));
                        minButton.setVisible(true);
                    } else { addButton.setText("+"); addButton.setBackground(Color.YELLOW); }
                }
            }
        });
        minButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton[0] == null) {
                    JOptionPane.showMessageDialog(choiceWindow,
                            "请先选择商品规格", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    Product productWithSpec = product.clone();
                    productWithSpec.setSpecialSpecification(selectedButton[0].getText());
                    for (int i = 0; i < shoppingCart.size(); i++) {
                        if (shoppingCart.get(i).equals(productWithSpec)) {
                            shoppingCart.remove(i); break; }
                    }
                    count[0]--;
                    updateCartCount();
                    if(count[0] == 0) {
                        addButton.setText("+"); addButton.setBackground(Color.YELLOW);
                        minButton.setVisible(false);
                    } else { addButton.setText(String.valueOf(count[0])); }
                }
            }
        });
        buttonPanel.add(addButton);
        buttonPanel.add(minButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    //搜索符合的商品面板
    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            updateProductPanel(categoryList.getSelectedValue());
            return;
        }

        productPanel.removeAll();
        JLabel searchResultLabel = new JLabel("搜索“" + searchText + "”结果：");
        searchResultLabel.setFont(new Font("黑体", Font.BOLD, 17));
        searchResultLabel.setForeground(new Color(120, 120, 120));
        searchResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        productPanel.add(searchResultLabel);
        productPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        boolean foundResults = false;
        List<Product> foundProducts = new ArrayList<>();
        for (String category : productMap.keySet()) {
            List<Product>products = productMap.get(category);
            if (products != null) {
                for (Product product : products) {
                    if (product.getName().toLowerCase().contains(searchText)) {
                        foundProducts.add(product);
                        foundResults = true; }
                }
            }
        }
        List<Product> sortedProducts = foundProducts.stream().sorted((p1, p2) -> {
            boolean p1SoldOut = p1.getStockAmount() == 0;
            boolean p2SoldOut = p2.getStockAmount() == 0;
            if (p1SoldOut && !p2SoldOut) { return 1; }
            else if (!p1SoldOut && p2SoldOut) { return -1; }
            return 0; }).collect(Collectors.toList());
        for (Product product : sortedProducts) {
            productPanel.add(createProductItemPanel(product));
            productPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        if (!foundResults) {
            JLabel noResultsLabel = new JLabel("没有找到匹配的商品");
            noResultsLabel.setFont(new Font("黑体", Font.PLAIN, 16));
            noResultsLabel.setForeground(new Color(120, 120, 120));
            noResultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            productPanel.add(noResultsLabel);
        }
        productPanel.add(Box.createVerticalGlue());
        productPanel.revalidate();
        productPanel.repaint();
    }

    //更新购物车右上角标识数字
    private void updateCartCount() {
        int count = shoppingCart.size();
        cartCountLabel.setText(String.valueOf(count));
        cartCountLabel.setVisible(count > 0);
        if (count > 0) { cartCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); }
    }

    //创建购物车窗口
    private void createCartWindow() {
        final JFrame cartWindow = new JFrame();
        cartWindow.setSize(500, 500);
        cartWindow.setLocationRelativeTo(this);
        cartWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cartWindow.setUndecorated(true);
        cartWindow.setShape(new RoundRectangle2D.Double(0, 0, 500, 500, 10, 10));

        final Image background = new ImageIcon("./lib/111暑期-vcampus/cart.jpg").getImage();
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10)) {
        	@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图片
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        	@Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(3));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                g2d.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cartWindow.setContentPane(contentPanel);
        cartWindow.setBackground(new Color(0, 0, 0, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        closeButtonPanel.setOpaque(false);
        closeButtonPanel.setPreferredSize(new Dimension(23, 23));
        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(23, 23));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cartWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
            }
        });
        closeButtonPanel.add(closeButton);
        topPanel.add(closeButtonPanel, BorderLayout.WEST);

        double totalw = 0;
        for (Product product : shoppingCart) { totalw += product.getWeight(); }
        titleLabel = new JLabel("  共"+shoppingCart.size()+"件商品  约" + String.format("%.1f", totalw) + "kg", SwingConstants.LEFT);
        titleLabel.setOpaque(false);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton clearButton = new JButton("清空购物车", new ImageIcon("./lib/111暑期-vcampus/trash.jpg"));
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        clearButton.setForeground(Color.BLACK);
        clearButton.setPreferredSize(new Dimension(130, 31));
        clearButton.setBackground(new Color(244, 244, 244));
        buttonPanel.add(clearButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shoppingCart.clear();
                updateCartCount();
                updateConfirmBuyButton(confirmBuyButton, 0);
                setGlassPaneVisible(mainMaskPanel, false);
                cartWindow.dispose();
                updateProductPanel(categoryList.getSelectedValue());
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        double total = getCartTotal(shoppingCart);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);
        JLabel totalLabel = new JLabel("  预估总计 ¥");
        totalLabel.setOpaque(false);
        totalLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        totalLabel2 = new JLabel(String.format("%.2f", total));
        totalLabel2.setOpaque(false);
        totalLabel2.setFont(new Font("微软雅黑", Font.BOLD, 19));
        confirmBuyButton = new RoundedButton((total >= 15 ? "去结算" : "15元起送"),
                (total >= 15 ? Color.YELLOW : Color.LIGHT_GRAY), (total >= 15 ? new Color(228, 228, 33) : Color.GRAY),
                (total >= 15 ? new Color(199, 199, 50) : Color.DARK_GRAY), Color.ORANGE, 15, 1);
        confirmBuyButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        confirmBuyButton.setPreferredSize(new Dimension(100, 40));
        confirmBuyButton.setEnabled(total >= 15);
        confirmBuyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	boolean isOutOfStock = false;
            	Map<String, CartItem> cartItemMap = new HashMap<>();
                for (Product product : shoppingCart) {
                    String key = product.getProductId();
                    if (cartItemMap.containsKey(key)) {
                        CartItem item = cartItemMap.get(key);
                        item.increaseQuantity();
                    } else { cartItemMap.put(key, new CartItem(product)); }
                }
                String name = "";
                for(CartItem item : cartItemMap.values()) {
                	if(item.getProduct().getStockAmount() - item.getQuantity() <= 0) {
                		name = item.getProduct().getName(); isOutOfStock = true; break; }
                }
                if(isOutOfStock) {
                	JOptionPane.showMessageDialog(cartWindow, name + "库存不足 请等待库存投放", "提示", JOptionPane.WARNING_MESSAGE);
                	return;
                }
                else {
                	cartWindow.dispose();
                    createBuyWindow();
                    clickCount = 0;
                }
            }
        });
        leftPanel.add(totalLabel);
        leftPanel.add(totalLabel2);
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(confirmBuyButton, BorderLayout.EAST);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        cartItemsPanel = new JPanel();
        cartItemsPanel.setOpaque(false);
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        customizeScrollBars(scrollPane);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        refreshCartPanel();
        cartWindow.setVisible(true);
    }
    //更新购物车确认购买按钮状态
    private void updateConfirmBuyButton(RoundedButton button, double total) {
        if(button == null) { return; }
        button.setText((total >= 15 ? "去结算" : "15元起送"));
        button.setEnabled(total >= 15);
        button.setBackgroundColor(total >= 15 ? Color.YELLOW : Color.LIGHT_GRAY);
        button.setHoverColor(total >= 15 ? new Color(228, 228, 33) : Color.GRAY);
        button.setPressedColor(total >= 15 ? new Color(199, 199, 50) : Color.DARK_GRAY);
    }

    //创建购物车窗口中的（每一个）商品面板
    private JPanel createCartItemPanel(final CartItem cartItem) {
        final Product product = cartItem.getProduct();

        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 230));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setMaximumSize(new Dimension(550, 110));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setOpaque(false);

        JLabel imageLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("图片加载失败");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            imageLabel.setPreferredSize(new Dimension(80, 80));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        imageLabel.setPreferredSize(new Dimension(80, 80));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(new Color(80, 80, 80));
        JLabel specLabel = new JLabel(product.getSpecification() == null ? "暂无规格" : product.getSpecification());
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        specLabel.setForeground(new Color(120, 120, 120));

        boolean isFirst = isFirstProductWithDiscount(product);
        boolean hasSale = getProductSale(product.getProductId()) != null;
        String priceText, totalText;
        if (isFirst && hasSale) {
            priceText = "第一件¥" + String.format("%.2f", getDiscountedPrice(product));
            totalText = "小计: ¥" + String.format("%.2f", getDiscountedPrice(product) +
                    product.getPrice() * (cartItem.getQuantity() - 1));
        } else {
            priceText = "¥" + String.format("%.2f", product.getPrice());
            totalText = "小计: ¥" + String.format("%.2f", product.getPrice() * cartItem.getQuantity());
        }

        JLabel priceLabel = new JLabel(priceText + " " + cartItem.getQuantity() + "件");
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(100, 100, 100));

        JLabel totalLabel = new JLabel(totalText);
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalLabel.setForeground(new Color(255, 60, 0));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(specLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(totalLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        //加/减按钮
        RoundedButton addButton = new RoundedButton("+", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.ORANGE, 10, 2);
        addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        addButton.setForeground(Color.BLACK);
        addButton.setPreferredSize(new Dimension(25, 25));
        RoundedButton minButton = new RoundedButton("-", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.ORANGE, 10, 2);
        minButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        minButton.setForeground(Color.BLACK);
        minButton.setPreferredSize(new Dimension(25, 25));
        addButton.addActionListener(new ActionListener() {  //添加按钮监听器
            public void actionPerformed(ActionEvent e) {
                shoppingCart.add(product);
                cartItem.increaseQuantity();
                refreshCartPanel();
                updateProductPanel(categoryList.getSelectedValue());
                double total = getCartTotal(shoppingCart);
                double totalw = 0;
                for (Product product : shoppingCart) { totalw += product.getWeight(); }
                updateCartCount();
                updateConfirmBuyButton(confirmBuyButton, total);
                titleLabel.setText("  共"+shoppingCart.size()+"件商品  约" + String.format("%.1f", totalw) + "kg");
                totalLabel2.setText(String.format("%.2f", total));
            }
        });
        minButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cartItem.decreaseQuantity();
                for (int i = 0; i < shoppingCart.size(); i++) {
                    if (shoppingCart.get(i).equals(product)) {
                        shoppingCart.remove(i);
                        break;
                    }
                }
                refreshCartPanel();
                updateProductPanel(categoryList.getSelectedValue());
                double total = getCartTotal(shoppingCart);
                double totalw = 0;
                for (Product product : shoppingCart) { totalw += product.getWeight(); }
                updateCartCount();
                updateConfirmBuyButton(confirmBuyButton, total);
                titleLabel.setText("  共"+shoppingCart.size()+"件商品  约" + String.format("%.1f", totalw) + "kg");
                totalLabel2.setText(String.format("%.2f", total));
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(minButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }
    //辅助方法：判断是否是第一个享受折扣的商品
    private boolean isFirstProductWithDiscount(Product product) {
        for (Product p : shoppingCart) {
            if (p.getName().equals(product.getName())) {
                return p.equals(product);
            }
        }
        return false;
    }
    //辅助方法：刷新购物车
    private void refreshCartPanel() {
        cartItemsPanel.removeAll();
        if (shoppingCart.isEmpty()) {
            JLabel emptyLabel = new JLabel("购物车为空", SwingConstants.CENTER);
            emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setOpaque(false);
            cartItemsPanel.add(emptyLabel);
        }
        else {
            Map<String, CartItem> cartItemMap = new HashMap<String, CartItem>();
            for (Product product : shoppingCart) {
                String key = product.getName() + "|" +
                        (product.getSpecification() != null ? product.getSpecification() : "");
                if (cartItemMap.containsKey(key)) {
                    CartItem item = cartItemMap.get(key);
                    item.increaseQuantity();
                } else {
                    cartItemMap.put(key, new CartItem(product));
                }
            }
            for (CartItem item : cartItemMap.values()) {
                cartItemsPanel.add(createCartItemPanel(item));
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    //创建确认购买窗口
    private void createBuyWindow(){
        buyWindow = new JFrame("购买界面");
        buyWindow.setSize(500, 750);
        buyWindow.setLocationRelativeTo(this);
        buyWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buyWindow.setUndecorated(true);
        buyWindow.setShape(new RoundRectangle2D.Double(0, 0, 500, 750, 10, 10));
        buyWindow.setVisible(true);

        maskPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        maskPanel.setOpaque(false);
        maskPanel.addMouseListener(new MouseAdapter() {});
        maskPanel.addMouseMotionListener(new MouseMotionAdapter() {});
        maskPanel.addKeyListener(new KeyAdapter() {});
        maskPanel.setFocusable(true);
        buyWindow.setGlassPane(maskPanel);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buyWindow.setContentPane(contentPanel);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        contentPanel.add(topPanel, BorderLayout.NORTH);

        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(23, 23));
        closeButton.setMaximumSize(new Dimension(23, 23));
        closeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
            }
        });
        JPanel buttonContentPanel = new JPanel(new BorderLayout(0, 5));
        buttonContentPanel.setOpaque(false);
        addressLabel = new JLabel("  选择收货地址");
        addressLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        addressLabel.setForeground(new Color(245, 165, 0));
        contactLabel = new JLabel("");
        contactLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        contactLabel.setForeground(Color.GRAY);
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(addressLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(contactLabel);
        buttonContentPanel.add(textPanel, BorderLayout.CENTER);
        JLabel arrowLabel = new JLabel(">  ");
        arrowLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        arrowLabel.setForeground(Color.GRAY);
        buttonContentPanel.add(arrowLabel, BorderLayout.EAST);
        chooseAddrButton = new RoundedButton("", Color.WHITE,
                Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.WHITE, 10, 0);
        chooseAddrButton.setPreferredSize(new Dimension(470, 40));
        chooseAddrButton.setLayout(new BorderLayout(10, 0));
        chooseAddrButton.add(buttonContentPanel, BorderLayout.CENTER);
        topPanel.add(closeButton);
        topPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        topPanel.add(chooseAddrButton);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        chooseTimeButton = new RoundedButton("", Color.WHITE,
                Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.WHITE, 10, 0);
        chooseTimeButton.setPreferredSize(new Dimension(470, 45));
        chooseTimeButton.setLayout(new BorderLayout(10, 0));
        JPanel buttonContentPanel2 = new JPanel(new BorderLayout(0, 5));
        buttonContentPanel2.setOpaque(false);
        wayLabel = new JLabel("   立即送出");
        wayLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        wayLabel.setForeground(Color.DARK_GRAY);
        timeLabel = new JLabel("");
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        timeLabel.setForeground(new Color(245, 165, 0));
        buttonContentPanel2.add(wayLabel, BorderLayout.WEST);
        buttonContentPanel2.add(timeLabel, BorderLayout.CENTER);
        chooseTimeButton.add(buttonContentPanel2, BorderLayout.CENTER);
        JLabel timeArrowLabel = new JLabel(">  ");
        timeArrowLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        timeArrowLabel.setForeground(Color.GRAY);
        buttonContentPanel2.add(timeArrowLabel, BorderLayout.EAST);
        chooseAddrButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(buyWindow, maskPanel, true);
                createAddressSelectionWindow(buyWindow);
            }
        });
        chooseTimeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(buyWindow, maskPanel, true);
                showTimeSelectionDialog(buyWindow);
            }
        });
        topPanel.add(chooseTimeButton);
        topPanel.add(Box.createRigidArea(new Dimension(0, 1)));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JPanel phonePanel = new JPanel(new BorderLayout(10, 0));
        phonePanel.setOpaque(false);
        phonePanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel phonePromptLabel = new JLabel("注意：如需到店自取请预留电话 -- ");
        phonePromptLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        phonePromptLabel.setForeground(new Color(70, 70, 70));
        phoneTextField = new JTextField();
        phoneTextField.setBorder(null);
        phoneTextField.setPreferredSize(new Dimension(150, 25));
        phoneTextField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        phonePanel.add(phonePromptLabel, BorderLayout.WEST);
        phonePanel.add(phoneTextField, BorderLayout.CENTER);

        JPanel phoneContainer = new JPanel(new BorderLayout());
        phoneContainer.setOpaque(false);
        phoneContainer.add(createSeparator(), BorderLayout.NORTH);
        phoneContainer.add(phonePanel, BorderLayout.CENTER);
        phoneContainer.add(createSeparator(), BorderLayout.SOUTH);
        centerPanel.add(phoneContainer, BorderLayout.NORTH);

        JPanel cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setOpaque(false);
        cartItemsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        Map<String, CartItem> cartItemMap = new HashMap<String, CartItem>();
        for (Product product : shoppingCart) {
            String key = product.getName() + "|" +
                    (product.getSpecification() != null ? product.getSpecification() : "");
            if (cartItemMap.containsKey(key)) {
                CartItem item = cartItemMap.get(key);
                item.increaseQuantity();
            } else { cartItemMap.put(key, new CartItem(product)); }
        }
        for (CartItem item : cartItemMap.values()) {
            cartItemsPanel.add(createBuyWindowCartItemPanel(item));
            cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        JScrollPane cartScrollPane = new JScrollPane(cartItemsPanel);
        customizeScrollBars(cartScrollPane);
        cartScrollPane.setOpaque(false);
        cartScrollPane.getViewport().setOpaque(false);
        cartScrollPane.setBorder(BorderFactory.createEmptyBorder());
        cartScrollPane.setPreferredSize(new Dimension(480, 100));
        centerPanel.add(cartScrollPane, BorderLayout.CENTER);

        JPanel extraPricePanel = new JPanel(new BorderLayout(10, 0));
        extraPricePanel.setOpaque(false);
        double total = getCartTotal(shoppingCart), originalTotal = 0;
        double totalw = 0;
        for (Product product : shoppingCart) {
            totalw += product.getWeight();
            originalTotal += product.getPrice();
        }
        double packPrice = shoppingCart.size() > 20 ? 1.5 : (shoppingCart.size() > 0 ? 1 : 0);
        double deliverPrice = totalw > 10 ? 4 : (totalw > 0 ? 2 : 0);
        double firstTotal = originalTotal + packPrice + deliverPrice;
        couponOff = 0; saleOff = originalTotal - total;
        JPanel tPanel = new JPanel(new BorderLayout(10, 0));
        JLabel tLabel = new JLabel("共"+shoppingCart.size()+"件商品  约" + String.format("%.1f", totalw) + "kg   ", SwingConstants.RIGHT);
        tLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        tLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        tLabel.setForeground(Color.GRAY);
        tPanel.add(createSeparator(), BorderLayout.NORTH);
        tPanel.add(tLabel, BorderLayout.CENTER);
        JPanel packPricePanel = new JPanel(new BorderLayout(10, 0));
        JLabel packLabel = new JLabel("  打包费");
        packLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JLabel packLabel2 = new JLabel("￥" + packPrice +"  ");
        packLabel2.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JPanel packPanel = new JPanel(new BorderLayout(10, 0));
        packPanel.add(packLabel, BorderLayout.WEST);
        packPanel.add(packLabel2, BorderLayout.EAST);
        packPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        packPricePanel.add(packPanel, BorderLayout.NORTH);
        JLabel ttLabel = new JLabel("  因商品较多 打包费上调");
        ttLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        ttLabel.setForeground(Color.GRAY);
        if(packPrice == 1.5) { ttLabel.setVisible(true); }
        else { ttLabel.setVisible(false); }
        packPricePanel.add(ttLabel, BorderLayout.SOUTH);
        JPanel deliverPricePanel = new JPanel(new BorderLayout(10, 0));
        JPanel deliverPanel = new JPanel(new BorderLayout(10, 0));
        JLabel deLabel = new JLabel("  配送费");
        deLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JLabel deLabel2 = new JLabel("￥" + deliverPrice + "  ");
        deLabel2.setFont(new Font("微软雅黑", Font.BOLD, 14));
        deliverPanel.add(deLabel, BorderLayout.WEST);
        deliverPanel.add(deLabel2, BorderLayout.EAST);
        deliverPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JLabel ttLabel2 = new JLabel("  因订单超重 配送费上调");
        ttLabel2.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        ttLabel2.setForeground(Color.GRAY);
        if(deliverPrice == 4) { ttLabel2.setVisible(true); }
        else { ttLabel2.setVisible(false); }
        deliverPricePanel.add(deliverPanel, BorderLayout.NORTH);
        deliverPricePanel.add(ttLabel2, BorderLayout.CENTER);
        deliverPricePanel.add(createSeparator(), BorderLayout.SOUTH);
        extraPricePanel.add(tPanel, BorderLayout.NORTH);
        extraPricePanel.add(packPricePanel, BorderLayout.CENTER);
        extraPricePanel.add(deliverPricePanel, BorderLayout.SOUTH);
        centerPanel.add(extraPricePanel, BorderLayout.SOUTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel tailPanel = new JPanel(new BorderLayout());
        tailPanel.setOpaque(false);
        JPanel discountPanel = new JPanel(new BorderLayout(10, 0));
        JLabel discountLabel = new JLabel(" 优惠明细");
        discountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        discountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JPanel couponPanel = new JPanel(new BorderLayout(10, 0));
        couponPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        JLabel cLabel = new JLabel("  抵扣券");
        cLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        arLabel = new JLabel(">");
        arLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        arLabel.setForeground(Color.GRAY);
        arLabel.setBorder(BorderFactory.createEmptyBorder(0, 330, 3, 0));
        arLabel.setVisible(false);
        cButton = new JButton("填写地址后可选");
        cButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 9));
        cButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        cButton.setForeground(Color.GRAY);
        cButton.setContentAreaFilled(false);
        cButton.setBorderPainted(false);
        cButton.setOpaque(false);
        cButton.setEnabled(false);
        cButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!cButton.isEnabled()) { return; }
                if (studentUser.myCouponList.isEmpty()) { return;  }
                double total = getCartTotal(shoppingCart);
                double packPrice = shoppingCart.size() > 20 ? 1.5 : (shoppingCart.size() > 0 ? 1 : 0);
                boolean hasAvailable = false;
                for (Coupon coupon : studentUser.myCouponList) {
                    if (coupon.getDueTime() != null && LocalDateTime.now().isAfter(coupon.getDueTime())) { continue; }
                    if (coupon.getCategory() != null) {
                        double categoryTotal = getCartTotal(coupon.getCategory());
                        if (categoryTotal + packPrice >= coupon.getSpendMoney()) { hasAvailable = true; break; }
                    } else {
                        if (total + packPrice >= coupon.getSpendMoney()) { hasAvailable = true; break; } } }
                if (!hasAvailable) { return; }
                setGlassPaneVisible(buyWindow, maskPanel, true);
                createCouponWindow(buyWindow);
            }
        });
        couponPanel.add(cLabel, BorderLayout.WEST);
        couponPanel.add(arLabel, BorderLayout.CENTER);
        couponPanel.add(cButton, BorderLayout.EAST);
        JPanel salePanel = new JPanel(new BorderLayout(10, 0));
        salePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        JLabel sLabel = new JLabel("  优惠活动");
        sLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JLabel sLabel2 = new JLabel("-￥" + String.format("%.2f", saleOff)+"  ");
        sLabel2.setFont(new Font("微软雅黑", Font.BOLD, 13));
        sLabel2.setForeground(new Color(255, 60, 0));
        salePanel.add(sLabel, BorderLayout.WEST);
        salePanel.add(sLabel2, BorderLayout.EAST);
        discountPanel.add(discountLabel, BorderLayout.NORTH);
        discountPanel.add(couponPanel, BorderLayout.CENTER);
        discountPanel.add(salePanel, BorderLayout.SOUTH);
        JPanel finalPanel = new JPanel(new BorderLayout());
        JPanel finalTotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        label1 = new JLabel("已优惠￥" + String.format("%.2f", saleOff + couponOff) + "  小计 ￥");
        label1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label2 = new JLabel(String.format("%.2f", firstTotal - saleOff - couponOff));
        label2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        finalTotalPanel.add(label1);
        finalTotalPanel.add(label2);
        finalPanel.add(createSeparator(), BorderLayout.NORTH);
        finalPanel.add(finalTotalPanel, BorderLayout.SOUTH);
        JPanel payPanel = new JPanel(new BorderLayout());
        JPanel markPanel = new JPanel(new BorderLayout());
        JLabel markLabel = new JLabel(" 备注");
        markLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        mButton = new JButton("请填写您的要求 >");
        mButton.setBorder(BorderFactory.createEmptyBorder(6, 0, 3, 5));
        mButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        mButton.setForeground(Color.GRAY);
        mButton.setContentAreaFilled(false);
        mButton.setBorderPainted(false);
        mButton.setOpaque(false);
        mButton.setPreferredSize(new Dimension(120, 20));
        mButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!mButton.getText().equals("请填写您的要求 >")) {
                    remarkTextArea.setText(mButton.getText());
                }
                createRemarkWindow(buyWindow);
                setGlassPaneVisible(buyWindow, maskPanel, true);
            }
        });
        markPanel.add(markLabel, BorderLayout.WEST);
        markPanel.add(mButton, BorderLayout.EAST);
        markPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel payFinalPanel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(50, 50, 50));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(50, 50, 50));
                g2d.setStroke(new BasicStroke(2));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                g2d.dispose();
            }
        };
        payFinalPanel.setMaximumSize(new Dimension(400, 60));
        payFinalPanel.setOpaque(false);
        priceLabel2 = new JLabel(" ￥" + String.format("%.2f", firstTotal - saleOff - couponOff));
        priceLabel2.setFont(new Font("微软雅黑", Font.BOLD, 20));
        priceLabel2.setForeground(Color.WHITE);
        RoundedButton payButton = new RoundedButton(Double.valueOf(label2.getText()) >= 15 ? "去付款": "15元起送",
                Double.valueOf(label2.getText()) >= 15 ? Color.YELLOW : Color.GRAY,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.WHITE, 10, 0);
        payButton.setForeground(new Color(50, 50, 50));
        payButton.setEnabled(Double.valueOf(label2.getText()) >= 15);
        payButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        payButton.setPreferredSize(new Dimension(100, 50));
        payButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(addressLabel.getText().equals("  选择收货地址") && (phoneTextField.getText().isEmpty() ||
                        !phoneTextField.getText().matches(".*\\d.*"))) {
                    setGlassPaneVisible(buyWindow, maskPanel, true);
                    createAddressSelectionWindow(buyWindow);
                }else {
                    buyWindow.dispose();
                    createPayWindow();
                }
            }
        });
        payFinalPanel.add(priceLabel2, BorderLayout.WEST);
        payFinalPanel.add(payButton, BorderLayout.EAST);
        payPanel.add(markPanel, BorderLayout.NORTH);
        payPanel.add(payFinalPanel, BorderLayout.SOUTH);
        tailPanel.add(discountPanel, BorderLayout.NORTH);
        tailPanel.add(finalPanel, BorderLayout.CENTER);
        tailPanel.add(payPanel, BorderLayout.SOUTH);
        contentPanel.add(tailPanel, BorderLayout.SOUTH);

    }
    //maskPanel辅助方法以及它的重载方法
    private void setGlassPaneVisible(JFrame frame, JPanel glassPane, boolean visible) {
        if(glassPane == null) return;
        if (visible) {
            glassPane.setVisible(true);
            glassPane.requestFocusInWindow(); //获取焦点以拦截键盘事件
        } else { glassPane.setVisible(false); }

        //递归禁用/启用所有组件
        setComponentsEnabled(frame.getContentPane(), !visible);
    }
    private void setGlassPaneVisible(JPanel glassPane, boolean visible) {
        if (visible) {
            glassPane.setVisible(true);
            glassPane.requestFocusInWindow(); //获取焦点以拦截键盘事件
        } else { glassPane.setVisible(false); }

        //递归禁用/启用所有组件
        setComponentsEnabled(getContentPane(), !visible);
    }
    //递归设置组件启用状态的辅助方法
    private void setComponentsEnabled(Container container, boolean enabled) {
        for (Component comp : container.getComponents()) {
            comp.setEnabled(enabled);
            if (comp instanceof Container) { setComponentsEnabled((Container) comp, enabled); }
        }
    }

    //创建地址选择窗口
    private void createAddressSelectionWindow(JFrame parent) {
        final JFrame addressWindow = new JFrame();
        addressWindow.setSize(400, 450);
        addressWindow.setLocationRelativeTo(parent);
        addressWindow.setUndecorated(true);
        addressWindow.setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 20, 20));
        addressWindow.setVisible(true);

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(Color.WHITE);
        addressWindow.setContentPane(dialogPanel);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("选择收货地址", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(23, 23));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addressWindow.dispose();
                setGlassPaneVisible(buyWindow, maskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        dialogPanel.add(titlePanel, BorderLayout.NORTH);


        DefaultListModel<Address> listModel = new DefaultListModel<>();
        for (Address address : studentUser.addressListModel) {
            listModel.addElement(address);
        }
        JList<Address> addressList = new JList<>(listModel);
        addressList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addressList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addressList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(5, 5));
                panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                panel.setOpaque(true);
                if (isSelected) { panel.setBackground(new Color(220, 240, 255)); }
                else { panel.setBackground(Color.WHITE); }
                if (value instanceof Address) {
                    Address address = (Address) value;
                    JLabel addressLabel = new JLabel(address.getAddr() + " " + address.getHouseNumber());
                    addressLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
                    addressLabel.setForeground(new Color(100, 100, 100));
                    JLabel nameLabel = new JLabel(address.getName() + "  " + address.getPhoneNumber());
                    nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                    nameLabel.setForeground(Color.GRAY);
                    panel.add(addressLabel, BorderLayout.NORTH);
                    panel.add(nameLabel, BorderLayout.CENTER);
                }
                return panel;
            }
        });
        JScrollPane scrollPane = new JScrollPane(addressList);
        customizeScrollBars(scrollPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        RoundedButton addButton = new RoundedButton("添加新的地址", new Color(255, 178, 102),
                new Color(255, 153, 51), new Color(255, 128, 0), new Color(0, 0, 0), 10, 0);
        addButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(110, 35));
        RoundedButton deleteButton = new RoundedButton("删除选中地址", Color.LIGHT_GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY, 10, 0);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        deleteButton.setPreferredSize(new Dimension(110, 35));
        RoundedButton confirmButton = new RoundedButton("选择该地址", new Color(255, 110, 110),
                new Color(255, 50, 50), new Color(200, 0, 0), new Color(0, 0, 0), 10, 0);
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        confirmButton.setPreferredSize(new Dimension(110, 38));
        confirmButton.setForeground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(confirmButton);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            JFrame addDialog = new JFrame();
            addDialog.setSize(350, 300);
            addDialog.setUndecorated(true);
            addDialog.setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 20, 20));
            addDialog.setLocationRelativeTo(addressWindow);
            addDialog.setLayout(new BorderLayout());
            addDialog.setVisible(true);

            JPanel dialogPanel2 = new JPanel(new BorderLayout(10, 10));
            dialogPanel2.setBorder(new EmptyBorder(15, 15, 15, 15));
            dialogPanel2.setBackground(new Color(240, 240, 240));
            addDialog.setContentPane(dialogPanel2);

            JPanel titlePanel2 = new JPanel(new BorderLayout());
            titlePanel2.setBackground(new Color(240, 240, 240));
            JLabel titleLabel2 = new JLabel("新增收货地址", JLabel.CENTER);
            titleLabel2.setFont(new Font("微软雅黑", Font.BOLD, 14));
            titleLabel2.setForeground(new Color(50, 50, 50));
            titlePanel2.add(titleLabel2, BorderLayout.CENTER);
            RoundedButton closeButton2 = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                    Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
            closeButton2.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton2.setForeground(Color.WHITE);
            closeButton2.setPreferredSize(new Dimension(20, 20));
            closeButton2.addActionListener(e2 -> addDialog.dispose());
            titlePanel2.add(closeButton2, BorderLayout.EAST);
            dialogPanel2.add(titlePanel2, BorderLayout.NORTH);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            JPanel addrPanel = createFieldPanel("地址:");
            JTextField addrField = createBorderlessTextField();
            addrPanel.add(addrField);
            contentPanel.add(addrPanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contentPanel.add(createSeparator());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            JPanel housePanel = createFieldPanel("门牌号:");
            JTextField houseField = createBorderlessTextField();
            housePanel.add(houseField);
            contentPanel.add(housePanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contentPanel.add(createSeparator());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            JPanel namePanel = createFieldPanel("联系人:");
            JTextField nameField = createBorderlessTextField();
            namePanel.add(nameField);
            contentPanel.add(namePanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contentPanel.add(createSeparator());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            JPanel phonePanel = createFieldPanel("手机号:");
            JTextField phoneField = createBorderlessTextField();
            phonePanel.add(phoneField);
            contentPanel.add(phonePanel);

            JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            RoundedButton cancelButton = new RoundedButton("取消", Color.LIGHT_GRAY, Color.GRAY,
                    Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
            cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            cancelButton.setPreferredSize(new Dimension(50, 30));
            RoundedButton saveButton = new RoundedButton("保存", Color.LIGHT_GRAY, Color.GRAY,
                    Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
            saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            saveButton.setPreferredSize(new Dimension(50, 30));
            cancelButton.addActionListener(ev -> addDialog.dispose());
            saveButton.addActionListener(ev -> {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String addr = addrField.getText().trim();
                String house = houseField.getText().trim();
                if (house.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                    JOptionPane.showMessageDialog(addDialog, "请填写必填字段", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Address newAddress = new Address();
                newAddress.setAddr(addr);
                newAddress.setHouseNumber(house);
                newAddress.setName(name);
                newAddress.setPhoneNumber(phone);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                    	shopClient.addAddress(newAddress);
                    	return null;
                    }

                    @Override
                    protected void done() {
                        try {
                        	get();
                        	studentUser.addressListModel.add(newAddress);
                            listModel.addElement(newAddress);
                        } catch (InterruptedException | ExecutionException e) {
                        	e.printStackTrace();
                        }
                    }
                }.execute();
                addDialog.dispose();
            });

            buttonPanel2.add(saveButton);
            buttonPanel2.add(cancelButton);
            addDialog.add(contentPanel, BorderLayout.CENTER);
            addDialog.add(buttonPanel2, BorderLayout.SOUTH);
        });
        deleteButton.addActionListener(e -> {
            int selectedIndex = addressList.getSelectedIndex();
            if (selectedIndex != -1) {
            	new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                    	shopClient.deleteAddress(addressList.getSelectedValue());
                    	return null;
                    }

                    @Override
                    protected void done() {
                        try {
                        	get();
                            studentUser.addressListModel.remove(selectedIndex);
                            listModel.remove(selectedIndex);
                        } catch (InterruptedException | ExecutionException e) {
                        	e.printStackTrace();
                        }
                    }
                }.execute();
            } else {
                JOptionPane.showMessageDialog(addressWindow, "请先选择一个地址", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        confirmButton.addActionListener(e -> {
            clickCount++;
            int selectedIndex = addressList.getSelectedIndex();
            if (selectedIndex != -1) {
                selectedAddress = studentUser.addressListModel.get(selectedIndex);
                if(selectedAddress != null) {
                    chooseAddrButton.setPreferredSize(new Dimension(470, 60));
                    addressLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
                    addressLabel.setText("  " + selectedAddress.getAddr() + " " + selectedAddress.getHouseNumber());
                    addressLabel.setForeground(Color.BLACK);
                    contactLabel.setText("    " + selectedAddress.getName() + " " + selectedAddress.getPhoneNumber());
                    if(studentUser.myCouponList.isEmpty()) { cButton.setEnabled(false); cButton.setText("暂无可用"); }
                    else {
                        boolean existBoth = false;
                        double total = getCartTotal(shoppingCart);
                        double packPrice = shoppingCart.size() > 20 ? 1.5 : (shoppingCart.size() > 0 ? 1 : 0);
                        double maxCouponOff = 0;
                        for(Coupon co : studentUser.myCouponList) {
                            boolean isTime = false, isCondition = false;
                            if ((co.getDueTime() == null) || (!LocalDateTime.now().isAfter(co.getDueTime()))) { isTime = true; }
                            String category = co.getCategory();
                            if (category != null) {
                                double cTotal = getCartTotal(category);
                                if (cTotal + packPrice >= co.getSpendMoney()) { isCondition = true; }
                            } else {
                                if (total + packPrice >= co.getSpendMoney()) { isCondition = true; }
                            }
                            if (isCondition && isTime) {
                                existBoth = true;
                                if (maxCouponOff < co.getOffMoney()) {
                                    maxCouponOff = co.getOffMoney();
                                    selectedCoupon = co;
                                }
                            }
                        }
                        if(existBoth) {
                            cButton.setText("-￥" + String.format("%.2f", maxCouponOff));
                            cButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
                            couponOff = maxCouponOff;
                            arLabel.setVisible(true);
                            label1.setText("已优惠￥" + String.format("%.2f", saleOff + couponOff) + "  小计 ￥");
                            if(clickCount == 1) {
                                label2.setText(String.format("%.2f", Double.valueOf(label2.getText()) - couponOff));
                                priceLabel2.setText(" ￥" + String.format("%.2f", Double.valueOf(label2.getText())));
                            }
                            cButton.setForeground(new Color(255, 60, 0));
                            cButton.setEnabled(true);
                        }
                        else { cButton.setEnabled(false); cButton.setText("暂无可用");  }
                    }
                }
                addressWindow.dispose();
                setGlassPaneVisible(buyWindow, maskPanel, false);
            } else {
                JOptionPane.showMessageDialog(addressWindow, "请先选择一个地址", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
    //辅助方法：创建无边框文本框
    private JTextField createBorderlessTextField() {
        JTextField textField = new JTextField();
        textField.setBorder(null);
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textField.setOpaque(false);
        return textField;
    }
    //辅助方法：创建字段面板
    private JPanel createFieldPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(50, 30));
        panel.add(label, BorderLayout.WEST);
        return panel;
    }
    //辅助方法：创建分隔线
    private Component createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(new Color(200, 200, 200));
        return separator;
    }

    //创建时间选择窗口
    private void showTimeSelectionDialog(JFrame parent) {
        JFrame timeWindow = new JFrame();
        timeWindow.setSize(400, 350);
        timeWindow.setLocationRelativeTo(parent);
        timeWindow.setUndecorated(true);
        timeWindow.setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 20, 20));
        timeWindow.setVisible(true);

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(Color.WHITE);
        timeWindow.setContentPane(dialogPanel);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("选择预计送达时间", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(23, 23));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeWindow.dispose();
                setGlassPaneVisible(buyWindow, maskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        dialogPanel.add(titlePanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(1);
        splitPane.setBackground(Color.WHITE);

        DefaultListModel<String> dateListModel = new DefaultListModel<>();
        JList<String> dateList = new JList<>(dateListModel);
        dateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dateList.setBackground(Color.WHITE);
        dateList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dateList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(new Color(240, 240, 240));
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
        JScrollPane dateScrollPane = new JScrollPane(dateList);
        customizeScrollBars(dateScrollPane);
        dateScrollPane.setBorder(BorderFactory.createEmptyBorder());
        dateScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dateScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        DefaultListModel<String> timeListModel = new DefaultListModel<>();
        JList<String> timeList = new JList<>(timeListModel);
        timeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timeList.setBackground(Color.WHITE);
        timeList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        timeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(new Color(240, 240, 240));
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
        JScrollPane timeScrollPane = new JScrollPane(timeList);
        customizeScrollBars(timeScrollPane);
        timeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        timeScrollPane.setBorder(BorderFactory.createEmptyBorder());
        timeScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        timeList.addMouseListener(new MouseAdapter() {  //添加双击选择时间的事件
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = timeList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedDate = dateList.getSelectedValue();
                        String selectedTime = timeListModel.getElementAt(index);
                        if ("今日已无配送时段".equals(selectedTime)) { return; }
                        if("立即送出".equals(selectedTime)) {
                            wayLabel.setText("   立即送出");
                            timeLabel.setText("");
                            setGlassPaneVisible(buyWindow, maskPanel, false);
                            timeWindow.dispose();
                        }
                        else {
                            wayLabel.setText("   指定时间       ");
                            timeLabel.setText(selectedDate + " " + selectedTime);
                            setGlassPaneVisible(buyWindow, maskPanel, false);
                            timeWindow.dispose();
                        }
                    }
                }
            }
        });

        splitPane.setLeftComponent(dateScrollPane);
        splitPane.setRightComponent(timeScrollPane);
        dialogPanel.add(splitPane, BorderLayout.CENTER);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日 (E)");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String currentDateStr = dateFormat.format(calendar.getTime());
        String currentTimeStr = timeFormat.format(calendar.getTime());
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) calendar.clone();
            day.add(Calendar.DATE, i);
            String dateStr = dateFormat.format(day.getTime());
            dateListModel.addElement(dateStr);
        }
        dateList.setSelectedIndex(0); //设置默认选中当日
        dateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = dateList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Calendar selectedDay = (Calendar) calendar.clone();
                    selectedDay.add(Calendar.DATE, selectedIndex);
                    String selectedDateStr = dateFormat.format(selectedDay.getTime());
                    boolean isToday = currentDateStr.equals(selectedDateStr);
                    updateTimeList(timeListModel, selectedDay, isToday, currentTimeStr);
                }
            }
        });
        updateTimeList(timeListModel, calendar, true, currentTimeStr);

        timeWindow.revalidate();
        timeWindow.repaint();
    }
    //更新右侧时间面板的方法
    private void updateTimeList(DefaultListModel<String> timeListModel, Calendar day,
                                boolean isToday, String currentTimeStr) {
        timeListModel.clear();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        int startHour = 0; int startMinute = 0;
        if (isToday) {  //如果是今天，从当前时间后的下一个整点或半点开始
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            startHour = currentHour + 1;
            if (currentMinute > 30) { startMinute = 30; }
            else if (currentMinute > 0) { startMinute = 0; }
            if (startHour >= 24) {  //如果当前时间接近午夜，可能需要调整
                timeListModel.addElement("今日已无配送时段");
            }
            else { timeListModel.addElement("立即送出"); }
        }
        for (int hour = startHour; hour < 24; hour++) {
            int minuteStart = (hour == startHour) ? startMinute : 0;
            for (int minute = minuteStart; minute < 60; minute += 30) {
                Calendar timeCal = (Calendar) day.clone();
                timeCal.set(Calendar.HOUR_OF_DAY, hour);
                timeCal.set(Calendar.MINUTE, minute);
                String timeStr = timeFormat.format(timeCal.getTime());
                timeListModel.addElement(timeStr);
                if (isToday && hour == 23 && minute == 30) { break; }
            }
        }

        if (timeListModel.isEmpty()) { timeListModel.addElement("今日已无配送时段"); }
    }

    //创建购买确认窗口中的（每一个）商品面板
    private JPanel createBuyWindowCartItemPanel(CartItem cartItem) {
        final Product product = cartItem.getProduct();
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 230));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(180, 180, 180));
                g2d.setStroke(new BasicStroke(2));
                int arcWidth = 10;
                int arcHeight = 10;
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                g2d.dispose();
            }
        };

        panel.setMaximumSize(new Dimension(470, 110));
        panel.setOpaque(false);

        JLabel imageLabel = new JLabel();
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        try {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("图片加载失败");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            imageLabel.setPreferredSize(new Dimension(80, 80));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        imageLabel.setPreferredSize(new Dimension(90, 80));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        nameLabel.setForeground(new Color(80, 80, 80));
        JLabel specLabel = new JLabel("规格: " + (product.getSpecification() == null ? "暂无" : product.getSpecification()));
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        specLabel.setForeground(new Color(100, 100, 100));
        boolean isFirst = isFirstProductWithDiscount(product);
        boolean hasSale = getProductSale(product.getProductId()) != null;
        String priceText, totalText;
        if (isFirst && hasSale) {
            priceText = "单价: ￥" + String.format("%.2f", getDiscountedPrice(product)) + " 第一件";
            totalText = "￥" + String.format("%.2f", getDiscountedPrice(product)
                    + product.getPrice() * (cartItem.getQuantity() - 1));
        } else {
            priceText = "单价: ￥" + String.format("%.2f", product.getPrice());
            totalText = "￥" + String.format("%.2f", product.getPrice() * cartItem.getQuantity());
        }
        JLabel priceLabel = new JLabel(priceText);
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        priceLabel.setForeground(new Color(100, 100, 100));
        JLabel quantityLabel = new JLabel("数量: " + cartItem.getQuantity());
        quantityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        quantityLabel.setForeground(new Color(100, 100, 100));
        JLabel tipLabel = new JLabel("7天无理由退货（拆封后不支持）");
        tipLabel.setFont(new Font("宋体", Font.BOLD, 13));
        tipLabel.setForeground(new Color(190, 190, 35));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(specLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(quantityLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(tipLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        totalPanel.setOpaque(false);
        JLabel totalLabel = new JLabel(totalText);
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        totalLabel.setForeground(new Color(255, 60, 0));
        totalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        totalPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        totalPanel.add(totalLabel);
        panel.add(totalPanel, BorderLayout.EAST);

        return panel;
    }

    //创建优惠券窗口
    private void createCouponWindow(JFrame parent) {
        JFrame couponWindow = new JFrame();
        couponWindow.setSize(430, 520);
        couponWindow.setLocationRelativeTo(parent);
        couponWindow.setUndecorated(true);
        couponWindow.setShape(new RoundRectangle2D.Double(0, 0, 430, 520, 20, 20));
        couponWindow.setVisible(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        couponWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("我的代金券", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                couponWindow.dispose();
                setGlassPaneVisible(buyWindow, maskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel enabledPanel = new JPanel(new BorderLayout());
        JLabel enabledLabel = new JLabel("可用代金券");
        enabledLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        enabledLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        customizeScrollBars(scrollPane);
        enabledPanel.add(enabledLabel, BorderLayout.NORTH);
        enabledPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel disabledPanel = new JPanel(new BorderLayout());
        JLabel disabledLabel = new JLabel("不可用代金券");
        disabledLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        disabledLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        JPanel mainPanel2 = new JPanel();
        mainPanel2.setLayout(new BoxLayout(mainPanel2, BoxLayout.Y_AXIS));
        JScrollPane scrollPane2 = new JScrollPane(mainPanel2);
        scrollPane2.setBorder(BorderFactory.createEmptyBorder());
        customizeScrollBars(scrollPane2);
        disabledPanel.add(disabledLabel, BorderLayout.NORTH);
        disabledPanel.add(scrollPane2, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(2);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(enabledPanel);
        splitPane.setBottomComponent(disabledPanel);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(new Label(" "), BorderLayout.SOUTH);

        double total = getCartTotal(shoppingCart);
        double packPrice = shoppingCart.size() > 20 ? 1.5 : (shoppingCart.size() > 0 ? 1 : 0);
        List<Coupon> sortedCoupons = new ArrayList<>(studentUser.myCouponList);
        Collections.sort(sortedCoupons, new Comparator<Coupon>() {
            @Override
            public int compare(Coupon c1, Coupon c2) {
                int result = Double.compare(c2.getOffMoney(), c1.getOffMoney());
                if (result == 0) { result = Double.compare(c2.getSpendMoney(), c1.getSpendMoney()); }
                return result;
            }
        });
        for (Coupon coupon : sortedCoupons) {
            if(!coupon.isUsed()) {
                boolean isTime = false, isCondition = false;
                if ((coupon.getDueTime() == null) || (!LocalDateTime.now().isAfter(coupon.getDueTime()))) { isTime = true; }
                String category = coupon.getCategory();
                if (category != null) {
                    double cTotal = getCartTotal(category);
                    if (cTotal + packPrice >= coupon.getSpendMoney()) { isCondition = true; }
                } else {
                    if (total + packPrice >= coupon.getSpendMoney()) { isCondition = true; }
                }
                if(isCondition && isTime) {
                    JPanel couponPanel = createCouponItemPanel(coupon, true);
                    couponPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    mainPanel.add(couponPanel);
                    couponPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            cButton.setText("-￥" + String.format("%.2f", coupon.getOffMoney()));
                            label1.setText("已优惠￥" + String.format("%.2f", saleOff + coupon.getOffMoney()) + "  小计 ￥");
                            label2.setText(String.format("%.2f", Double.valueOf(label2.getText()) + couponOff - coupon.getOffMoney()));
                            priceLabel2.setText(" ￥" + String.format("%.2f", Double.valueOf(label2.getText())));
                            couponOff = coupon.getOffMoney();
                            selectedCoupon = coupon;
                            couponWindow.dispose();
                            setGlassPaneVisible(buyWindow, maskPanel, false);
                        }
                    });
                } else {
                    JPanel couponPanel = createCouponItemPanel(coupon, false);
                    couponPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    mainPanel2.add(couponPanel);
                }
            }

        }
    }
    //创建优惠券窗口的（每一个）代金券面板
    private JPanel createCouponItemPanel(Coupon coupon, boolean isEnabled) {
        JPanel panel = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled) {
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 245, 230),
                            getWidth(), getHeight(), new Color(255, 250, 245));
                    g2d.setPaint(gradient);
                } else { g2d.setColor(new Color(245, 245, 245)); }
                int arcWidth = 15;
                int arcHeight = 15;
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
                // 添加轻微阴影效果
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arcWidth, arcHeight);
            }
        };
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));
        panel.setMaximumSize(new Dimension(450, 100));
        panel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        JLabel nameLabel = new JLabel("  " + coupon.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        nameLabel.setForeground(isEnabled ? new Color(50, 50, 50) : new Color(150, 150, 150));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(nameLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        JLabel timeLabel = new JLabel(coupon.getDueTime() == null ? "" :
                (coupon.getDueTime().toLocalDate().equals(LocalDate.now()) ? "  今日到期" :
                        (coupon.getDueTime().toLocalDate().equals(LocalDate.now().plusDays(1)) ? "  明日到期" :
                                ("  有效期至：" + new SimpleDateFormat("yyyy-MM-dd HH:mm")
                                        .format(java.sql.Timestamp.valueOf(coupon.getDueTime()))))));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        timeLabel.setForeground(isEnabled ? new Color(250, 100, 70) : new Color(180, 180, 180));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(timeLabel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel valueLabel = new JLabel("¥" + coupon.getOffMoney() + "  ");
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        valueLabel.setForeground(isEnabled ? new Color(255, 100, 50) : new Color(180, 180, 180));
        valueLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel valueLabel2 = new JLabel("满" + coupon.getSpendMoney() + "可用   ");
        valueLabel2.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        valueLabel2.setForeground(isEnabled ? new Color(255, 150, 50) : new Color(180, 180, 180));
        valueLabel2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(valueLabel);
        rightPanel.add(Box.createVerticalStrut(2));
        rightPanel.add(valueLabel2);
        topPanel.add(leftPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(new Color(220, 220, 220, 150));

        JLabel descLabel = new JLabel("  " + (coupon.getCategory() == null ? "在线支付专享" :
                coupon.getCategory() + "类专享"));
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        descLabel.setForeground(isEnabled ? new Color(120, 120, 120) : new Color(180, 180, 180));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(separator, BorderLayout.CENTER);
        panel.add(descLabel, BorderLayout.SOUTH);

        return panel;
    }

    //创建备注栏窗口
    private void createRemarkWindow(JFrame parent) {
        JFrame remarkWindow = new JFrame();
        remarkWindow.setSize(300, 300);
        remarkWindow.setLocationRelativeTo(parent);
        remarkWindow.setUndecorated(true);
        remarkWindow.setShape(new RoundRectangle2D.Double(0, 0, 300, 300, 20, 20));
        remarkWindow.setVisible(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        remarkWindow.setContentPane(contentPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("备注栏", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        RoundedButton closeButton = new RoundedButton("×", new Color(220, 220, 220),
                new Color(200, 200, 200), new Color(180, 180, 180), Color.GRAY, 15, 0);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                remarkWindow.dispose();
                setGlassPaneVisible(buyWindow, maskPanel, false);
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel textAreaPanel = new JPanel(new BorderLayout(5, 5));
        textAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        remarkTextArea = new JTextArea();
        if(!mButton.getText().equals("请填写您的要求 >")) {
            remarkTextArea.setText(mButton.getText());
        }
        remarkTextArea.setLineWrap(true);
        remarkTextArea.setWrapStyleWord(true);
        remarkTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        remarkTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        PlainDocument doc = (PlainDocument) remarkTextArea.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {  //限制输入长度为50个字符
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() - length + text.length();
                if (newLength <= 50) { super.replace(fb, offset, length, text, attrs); }
            }
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() + text.length();
                if (newLength <= 50) { super.insertString(fb, offset, text, attrs); }
            }
        });
        JScrollPane textScrollPane = new JScrollPane(remarkTextArea);
        customizeScrollBars(textScrollPane);
        textScrollPane.setBorder(BorderFactory.createEmptyBorder());
        textAreaPanel.add(textScrollPane, BorderLayout.CENTER);

        JLabel charCountLabel = new JLabel(remarkTextArea.getText().length() + "/50");
        charCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        charCountLabel.setForeground(new Color(150, 150, 150));
        charCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        charCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        textAreaPanel.add(charCountLabel, BorderLayout.SOUTH);
        remarkTextArea.getDocument().addDocumentListener(new DocumentListener() {  //添加文本变化监听器，更新字符计数
            @Override
            public void insertUpdate(DocumentEvent e) { updateCharCount(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateCharCount(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateCharCount(); }
            private void updateCharCount() {
                int length = remarkTextArea.getText().length();
                charCountLabel.setText(length + "/50");
                if (length >= 45) { charCountLabel.setForeground(new Color(255, 100, 0)); }
                else { charCountLabel.setForeground(new Color(150, 150, 150)); }
            }
        });

        contentPanel.add(textAreaPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        RoundedButton confirmButton = new RoundedButton("确定", new Color(255, 178, 102),
                new Color(255, 153, 51), new Color(255, 128, 0), new Color(0, 0, 0), 10, 0);
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(70, 30));
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPaneVisible(buyWindow, maskPanel, false);
                if(Pattern.compile("\\p{Script=Han}").matcher(remarkTextArea.getText()).find() ||
                        Pattern.compile("[a-zA-Z]").matcher(remarkTextArea.getText()).find()) {
                    mButton.setText(remarkTextArea.getText());
                }
                else { mButton.setText("请填写您的要求 >"); }
                remarkWindow.dispose();
            }
        });
        buttonPanel.add(confirmButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

    }

    //创建支付窗口
    private void createPayWindow() {
        JFrame payWindow = new JFrame();
        payWindow.setSize(400, 400);
        payWindow.setLocationRelativeTo(this);
        payWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        payWindow.setUndecorated(true);
        payWindow.setShape(new RoundRectangle2D.Double(0, 0, 400, 400, 20, 20));
        payWindow.setVisible(true);

        JPanel payPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.GRAY);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        payPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        payPanel.setBackground(Color.WHITE);
        payWindow.setContentPane(payPanel);

        subMaskPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        subMaskPanel.setOpaque(false);
        subMaskPanel.addMouseListener(new MouseAdapter() {});
        subMaskPanel.addMouseMotionListener(new MouseMotionAdapter() {});
        subMaskPanel.addKeyListener(new KeyAdapter() {});
        subMaskPanel.setFocusable(true);
        payWindow.setGlassPane(subMaskPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("支付界面-请尽快完成支付", JLabel.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 80, 5, 5));
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        titleLabel.setForeground(new Color(50, 50, 50));
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        cancelButton.setForeground(Color.GRAY);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(false);
        cancelButton.setPreferredSize(new Dimension(80, 20));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                payWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
                JOptionPane.showMessageDialog(payWindow,
                        "订单已取消", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(cancelButton, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        JPanel pricePanel = new JPanel(new GridBagLayout());
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        JLabel toPayLabel = new JLabel(priceLabel2.getText() + "   ");
        toPayLabel.setFont(new Font("微软雅黑", Font.BOLD, 25));
        toPayLabel.setForeground(new Color(50, 50, 50));
        pricePanel.add(toPayLabel);

        JPanel choosePanel = new JPanel(new GridBagLayout());
        choosePanel.setBackground(Color.WHITE);
        choosePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        RoundedButton bankButton = new RoundedButton("使用银行支付", new Color(255, 245, 180),
                new Color(255, 235, 160), new Color(255, 225, 140), Color.GRAY, 15, 1);
        bankButton.setPreferredSize(new Dimension(120, 40));
        bankButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        bankButton.setForeground(new Color(200, 159, 31));
        selectedButton[0] = bankButton;
        RoundedButton balanceButton = new RoundedButton("使用余额支付", Color.WHITE, Color.LIGHT_GRAY,
                Color.GRAY, Color.GRAY, 15, 1);
        balanceButton.setPreferredSize(new Dimension(120, 40));
        balanceButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        choosePanel.add(bankButton, gbc);
        choosePanel.add(balanceButton, gbc);
        JLabel balanceLabel = new JLabel("账户余额：" + String.format("%.2f", studentUser.getBalanceShop()), JLabel.CENTER);
        balanceLabel.setBackground(Color.WHITE);
        balanceLabel.setBorder(new EmptyBorder(3, 0, 10, 0));
        balanceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        balanceLabel.setForeground(Color.GRAY);
        balanceLabel.setVisible(false);
        JPanel bPanel = new JPanel(new BorderLayout());
        bPanel.setBackground(Color.WHITE);
        bPanel.add(choosePanel, BorderLayout.NORTH);
        bPanel.add(balanceLabel, BorderLayout.CENTER);

        bankButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                balanceButton.setBackgroundColor(Color.WHITE);
                balanceButton.setHoverColor(Color.LIGHT_GRAY);
                balanceButton.setPressedColor(Color.GRAY);
                balanceButton.setForeground(Color.BLACK);
                bankButton.setBackgroundColor(new Color(255, 245, 180));
                bankButton.setHoverColor(new Color(255, 235, 160));
                bankButton.setPressedColor(new Color(255, 225, 140));
                bankButton.setForeground(new Color(200, 159, 31));
                selectedButton[0] = bankButton;
                bankButton.repaint();
                balanceLabel.setVisible(false);
            }
        });
        balanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bankButton.setBackgroundColor(Color.WHITE);
                bankButton.setHoverColor(Color.LIGHT_GRAY);
                bankButton.setPressedColor(Color.GRAY);
                bankButton.setForeground(Color.BLACK);
                balanceButton.setBackgroundColor(new Color(255, 245, 180));
                balanceButton.setHoverColor(new Color(255, 235, 160));
                balanceButton.setPressedColor(new Color(255, 225, 140));
                balanceButton.setForeground(new Color(200, 159, 31));
                selectedButton[0] = balanceButton;
                balanceButton.repaint();
                balanceLabel.setVisible(true);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        RoundedButton toPayButton = new RoundedButton("确认支付", Color.YELLOW,
                new Color(255, 220, 0), new Color(255, 180, 0), Color.WHITE, 15, 0);
        toPayButton.setForeground(new Color(50, 50, 50));
        toPayButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        toPayButton.setPreferredSize(new Dimension(180, 45));
        toPayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectedButton[0] == bankButton) {
                    //联动银行
                	ShopBankPaymentDialog paymentDialog = new ShopBankPaymentDialog(payWindow, new BigDecimal(
                			Double.parseDouble(label2.getText())), new ShopBankPaymentDialog.PaymentCallback() {
                	            @Override
                	            public void onPaymentSuccess(BigDecimal amount) {
                	                // 支付成功后的处理
                	            	payWindow.dispose();
                    	            createOrderWindow();
                	            }
                	            @Override
                	            public void onPaymentFailure(String errorMessage) {
                	            	JOptionPane.showMessageDialog(payWindow, "订单未完成！", "提示", JOptionPane.INFORMATION_MESSAGE);
                	            	payWindow.dispose();
                    	            setGlassPaneVisible(mainMaskPanel, false);
                	            }
                	        });
                	    paymentDialog.setVisible(true);
                	
                }
                else {
                    if(studentUser.getBalanceShop() < Double.parseDouble(label2.getText())) {
                        JOptionPane.showMessageDialog(payWindow, "余额不足！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        setGlassPaneVisible(payWindow, subMaskPanel, true);
                        createPayPanel(payWindow);
                    }
                }
            }
        });
        buttonPanel.add(toPayButton);
        centerPanel.add(pricePanel, BorderLayout.NORTH);
        centerPanel.add(bPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        payPanel.add(titlePanel, BorderLayout.NORTH);
        payPanel.add(centerPanel, BorderLayout.CENTER);
    }
    //创建密码键盘
    private void createPayPanel(JFrame parent) {
        JFrame payPanel = new JFrame();
        payPanel.setSize(300, 400);
        payPanel.setLocationRelativeTo(parent);
        payPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        payPanel.setUndecorated(true);
        payPanel.setShape(new RoundRectangle2D.Double(0, 0, 300, 400, 20, 20));
        payPanel.setVisible(true);

        JPanel layPanel = new JPanel(new BorderLayout(10, 10));
        layPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        layPanel.setBackground(Color.WHITE);
        payPanel.setContentPane(layPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel titleLabel = new JLabel("         请输入支付密码", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        cancelButton.setForeground(Color.GRAY);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(false);
        cancelButton.setPreferredSize(new Dimension(60, 20));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                payPanel.dispose();
                setGlassPaneVisible(parent, subMaskPanel, false);
                JOptionPane.showMessageDialog(payPanel,
                        "支付已取消", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(cancelButton, BorderLayout.EAST);

        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        JPasswordField[] passwordDots = new JPasswordField[6];
        for (int i = 0; i < 6; i++) {
            passwordDots[i] = new JPasswordField(1);
            passwordDots[i].setHorizontalAlignment(JTextField.CENTER);
            passwordDots[i].setFont(new Font("Arial", Font.BOLD, 20));
            passwordDots[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            passwordDots[i].setBackground(Color.WHITE);
            passwordDots[i].setEditable(false);
            passwordDots[i].setFocusable(false);
            passwordDots[i].setPreferredSize(new Dimension(40, 40));
            gbc.gridx = i;
            gbc.gridy = 0;
            passwordPanel.add(passwordDots[i], gbc);
        }
        JPanel keypadPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        keypadPanel.setBackground(Color.WHITE);
        keypadPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JButton[] numberButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            numberButtons[i] = new RoundedButton(String.valueOf(i), Color.WHITE,
                    new Color(240, 240, 240), new Color(220, 220, 220), Color.BLACK, 10, 1);
            numberButtons[i].setFont(new Font("Arial", Font.BOLD, 18));
            numberButtons[i].setPreferredSize(new Dimension(60, 50));
        }
        JButton backButton = new RoundedButton("←", Color.WHITE,
                new Color(240, 240, 240), new Color(220, 220, 220), Color.BLACK, 10, 1);
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setPreferredSize(new Dimension(60, 50));
        JButton confirmButton = new RoundedButton("确定", new Color(76, 175, 80),
                new Color(69, 160, 73), new Color(56, 142, 60), Color.WHITE, 10, 0);
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        confirmButton.setPreferredSize(new Dimension(60, 50));

        keypadPanel.add(numberButtons[1]); keypadPanel.add(numberButtons[2]); keypadPanel.add(numberButtons[3]);
        keypadPanel.add(numberButtons[4]); keypadPanel.add(numberButtons[5]); keypadPanel.add(numberButtons[6]);
        keypadPanel.add(numberButtons[7]); keypadPanel.add(numberButtons[8]); keypadPanel.add(numberButtons[9]);
        keypadPanel.add(backButton); keypadPanel.add(numberButtons[0]); keypadPanel.add(confirmButton);

        StringBuilder enteredPassword = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            final int digit = i;
            numberButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (enteredPassword.length() < 6) {
                        enteredPassword.append(digit);
                        updatePasswordDots(passwordDots, enteredPassword.toString());
                    }
                }
            });
        }
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (enteredPassword.length() > 0) {
                    enteredPassword.setLength(enteredPassword.length() - 1);
                    updatePasswordDots(passwordDots, enteredPassword.toString());
                }
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (enteredPassword.length() < 6) {
                    JOptionPane.showMessageDialog(payPanel, "请输入6位支付密码", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (enteredPassword.toString().equals(studentUser.getPasswordShop())) {
                    payPanel.dispose();
                    setGlassPaneVisible(parent, subMaskPanel, false);
                    parent.dispose();
                    createOrderWindow();
                    JOptionPane.showMessageDialog(payPanel, "支付成功!", "提示", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(payPanel, "支付密码错误，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                    enteredPassword.setLength(0);
                    updatePasswordDots(passwordDots, "");
                }
            }
        });
        layPanel.add(titlePanel, BorderLayout.NORTH);
        layPanel.add(passwordPanel, BorderLayout.CENTER);
        layPanel.add(keypadPanel, BorderLayout.SOUTH);
    }
    //密码键盘辅助方法
    private void updatePasswordDots(JPasswordField[] dots, String password) {
        for (int i = 0; i < dots.length; i++) {
            if (i < password.length()) {
                dots[i].setText("•");
                dots[i].setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
            } else {
                dots[i].setText("");
                dots[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); }
        }
    }
    
    public void updateCouponOperation() {
    	if(selectedCoupon != null) {
            selectedCoupon.setUsed(true);
            if(selectedCoupon.getCouponId().equals(couponList.get(0).getCouponId())) {
                button1.setEnabled(false);
                button1.setText(couponList.get(0).getName() + " " + (int)couponList.get(0).getSpendMoney()
                        + "减" + (int)couponList.get(0).getOffMoney() + " | 已使用");
            }
            if(selectedCoupon.getCouponId().equals(couponList.get(1).getCouponId())) {
                button2.setEnabled(false);
                button2.setText(couponList.get(1).getName() + " " + (int)couponList.get(1).getSpendMoney()
                        + "减" + (int)couponList.get(1).getOffMoney() + " | 已使用");
            }
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                	shopClient.updateUserCoupon(selectedCoupon.getCouponId());
                	return null;
                }

                @Override
                protected void done() {
                    try {
                    	get();
                    	return;
                    } catch (InterruptedException | ExecutionException e) {
                    	e.printStackTrace();
                    }
                }
            }.execute();
        }
    }
    //创建订单窗口
    private void createOrderWindow() {
        JFrame orderWindow = new JFrame();
        orderWindow.setSize(450, 700);
        orderWindow.setLocationRelativeTo(this);
        orderWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        orderWindow.setUndecorated(true);
        orderWindow.setShape(new RoundRectangle2D.Double(0, 0, 450, 700, 20, 20));
        orderWindow.setVisible(true);

        Order order = new Order();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        orderWindow.setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("订单详情", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));

        RoundedButton closeButton = new RoundedButton("×", Color.LIGHT_GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.GRAY, 12, 1);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(23, 23));
        closeButton.addActionListener(e -> {
            //支付成功后操作
            Map<String, CartItem> cartItemMap2 = new HashMap<String, CartItem>();
            for (Product product : shoppingCart) {
                String key = product.getProductId();
                if (cartItemMap2.containsKey(key)) {
                    CartItem item = cartItemMap2.get(key);
                    item.increaseQuantity();
                } else { cartItemMap2.put(key, new CartItem(product)); }
            }

            for (String key : cartItemMap2.keySet()) {
                CartItem item = cartItemMap2.get(key);
                Product originalProduct = productIdMap.get(key); //获取原始商品对象
                if (originalProduct != null) {
                    boolean hasSale = getProductSale(originalProduct.getProductId()) != null;
                    double smallTotal;
                    if (hasSale) {
                        smallTotal = getDiscountedPrice(originalProduct)
                                + originalProduct.getPrice() * (item.getQuantity() - 1);
                    } else {
                        smallTotal = originalProduct.getPrice() * item.getQuantity();
                    }
                    order.items.add(new OrderItem(order.orderId, originalProduct.getProductId(),
                            item.getQuantity(), originalProduct.getPrice(), smallTotal));
                    
                    originalProduct.addSoldAmount(item.getQuantity());
                    originalProduct.minusStockAmount(item.getQuantity());
                    
                }
            }
            shoppingCart.clear();
            updateCartCount();
            updateConfirmBuyButton(confirmBuyButton, 0);
            updateProductPanel(categoryList.getSelectedValue());
            if(selectedButton[0].getText().equals("使用余额支付")) {
            	studentUser.setBalanceShop(studentUser.getBalanceShop() - Double.parseDouble(label2.getText()));
            }
            studentUser.setPoints((int)(Double.parseDouble(label2.getText())*10));
            studentUser.historyOrders.add(order);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                	shopClient.updateShopProfile(studentUser);
                	shopClient.createOrder(order);
                	for(OrderItem item : order.items) {
                		int newStock = shopClient.getProductById(item.productId).getStockAmount() - item.quantity;
                		shopClient.updateProductStock(item.productId, newStock);
                		int newSold = shopClient.getProductById(item.productId).getSoldAmount() + item.quantity;
                		shopClient.updateProductSales(item.productId, newSold);
                	}
                	updateCouponOperation();
                	return null;
                }
                @Override
                protected void done() {
                    try {
                    	get();
                    	orderWindow.dispose();
                        setGlassPaneVisible(mainMaskPanel, false);
                    } catch (InterruptedException | ExecutionException e) {
                    	e.printStackTrace();
                    }
                }
            }.execute();
            
            
        });

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);


        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M月d日 (E) HH:mm", Locale.CHINA);
        LocalDateTime time;
        if (wayLabel.getText().equals("   立即送出")) { time = LocalDateTime.now();
        } else {
            String timeText = timeLabel.getText().replaceAll("\\(.*?\\) ", "");
            int currentYear = LocalDate.now().getYear();
            String timeTextWithYear = currentYear + "年" + timeText;
            DateTimeFormatter fmtWithYear = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm", Locale.CHINA);
            time = LocalDateTime.parse(timeTextWithYear, fmtWithYear);
        }
        LocalDateTime estimatedArrival = time.plusMinutes(20);
        LocalDateTime estimatedArrival2 = time.plusMinutes(35);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate today = LocalDate.now();
        LocalDate arrivalDate = estimatedArrival.toLocalDate();
        String dateDisplay;
        if (arrivalDate.equals(today)) { dateDisplay = "今日"; }
        else if (arrivalDate.equals(today.plusDays(1))) { dateDisplay = "明日"; }
        else { dateDisplay = arrivalDate.format(DateTimeFormatter.ofPattern("M月d日")); }
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.setBackground(Color.WHITE);
        JLabel timeLabel1 = new JLabel(dateDisplay + " " + estimatedArrival.format(timeFormatter)
                + "-" + estimatedArrival2.format(timeFormatter) + " 预计送达");
        timeLabel1.setFont(new Font("微软雅黑", Font.BOLD, 16));
        timeLabel1.setForeground(new Color(120, 120, 120));
        timePanel.add(timeLabel1);
        contentPanel.add(timePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel infoCard = createCardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        JLabel infoTitle = new JLabel("订单信息");
        infoTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        infoTitle.setForeground(new Color(70, 70, 70));
        infoTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        infoCard.add(infoTitle);
        String orderId = generateOrderNumber();
        LocalDateTime createTime = LocalDateTime.now();
        String address = addressLabel.getText().trim();
        String payWay = selectedButton[0].getText();
        double finalTotal = Double.parseDouble(label2.getText());
        order.orderId = orderId;
        order.userId = studentUser.userId;
        order.finalAmount = finalTotal;
        order.createTime = createTime;
        order.expectTime = estimatedArrival2;
        order.address = address;
        order.payWay = payWay;
        infoCard.add(createInfoItem(" 订单号码", orderId, true));
        infoCard.add(createInfoItem(" 下单时间", createTime.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false));
        JPanel addPanel = new JPanel(new BorderLayout(10, 0));
        addPanel.setOpaque(false); addPanel.setMaximumSize(new Dimension(400, 25));
        JLabel addTitleLabel = new JLabel(" 配送地址");
        addTitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addTitleLabel.setForeground(new Color(100, 100, 100));
        addTitleLabel.setPreferredSize(new Dimension(80, 20));
        JLabel addContentLabel = new JLabel(address);
        addContentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        addContentLabel.setForeground(new Color(120, 120, 120));
        addPanel.add(addTitleLabel, BorderLayout.WEST);
        addPanel.add(addContentLabel, BorderLayout.CENTER);
        infoCard.add(addPanel);
        infoCard.add(createInfoItem(" 期望时间", estimatedArrival2.format(fmt) , false));
        infoCard.add(createInfoItem(" 支付方式", payWay + "（实付¥" + label2.getText() + "）", false));
        infoCard.add(createInfoItem(" 备注", mButton.getText().equals("请填写您的要求 >") ?
                "无" : remarkTextArea.getText(), false));
        contentPanel.add(infoCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel productsCard = createCardPanel();
        productsCard.setLayout(new BoxLayout(productsCard, BoxLayout.Y_AXIS));
        JLabel productsTitle = new JLabel("商品清单");
        productsTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        productsTitle.setForeground(new Color(70, 70, 70));
        productsTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        productsCard.add(productsTitle);
        Map<String, CartItem> cartItemMap = new HashMap<>();
        for (Product product : shoppingCart) {
            String key = product.getName() + "|" +
                    (product.getSpecification() != null ? product.getSpecification() : "");
            if (cartItemMap.containsKey(key)) {
                CartItem item = cartItemMap.get(key);
                item.increaseQuantity();
            } else { cartItemMap.put(key, new CartItem(product)); }
        }
        for (CartItem item : cartItemMap.values()) {
            productsCard.add(createProductItem(item));
            productsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        contentPanel.add(productsCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel costCard = createCardPanel();
        costCard.setLayout(new BoxLayout(costCard, BoxLayout.Y_AXIS));
        JLabel costTitle = new JLabel("费用明细");
        costTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        costTitle.setForeground(new Color(70, 70, 70));
        costTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        costCard.add(costTitle);
        double total = getCartTotal(shoppingCart);
        double packPrice = shoppingCart.size() > 20 ? 1.5 : (shoppingCart.size() > 0 ? 1 : 0);
        double deliverPrice = 0;
        double totalw = 0;
        for (Product product : shoppingCart) { totalw += product.getWeight(); }
        deliverPrice = totalw > 10 ? 4 : (totalw > 0 ? 2 : 0);
        order.Off = saleOff + couponOff;
        costCard.add(createCostItem(" 商品费用", "¥" + String.format("%.2f", total)));
        costCard.add(Box.createRigidArea(new Dimension(0, 3)));
        costCard.add(createCostItem(" 打包费", "¥" + String.format("%.2f", packPrice)));
        costCard.add(Box.createRigidArea(new Dimension(0, 3)));
        costCard.add(createCostItem(" 配送费", "¥" + String.format("%.2f", deliverPrice)));
        costCard.add(Box.createRigidArea(new Dimension(0, 3)));
        costCard.add(createCostItem(" 优惠券抵扣", "-¥" + String.format("%.2f", couponOff)));
        costCard.add(Box.createRigidArea(new Dimension(0, 5)));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(220, 220, 220));
        costCard.add(separator);
        costCard.add(Box.createRigidArea(new Dimension(0, 5)));
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        JLabel totalLabel = new JLabel("合计");
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        JLabel totalValue = new JLabel("¥" + String.format("%.2f", total + packPrice + deliverPrice - couponOff));
        totalValue.setFont(new Font("微软雅黑", Font.BOLD, 16));
        totalValue.setForeground(new Color(255, 100, 0));
        JLabel discountLabel = new JLabel("已优惠¥" + String.format("%.2f", saleOff + couponOff) + "  ");
        discountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        discountLabel.setForeground(new Color(150, 150, 150));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(totalValue);
        rightPanel.add(discountLabel);
        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(rightPanel, BorderLayout.EAST);
        costCard.add(totalPanel);
        contentPanel.add(costCard);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(new EmptyBorder(15, 0, 5, 0));
        RoundedButton modifyBtn = new RoundedButton("修改地址", Color.WHITE,
                new Color(240, 240, 240), new Color(220, 220, 220), Color.GRAY, 8, 1);
        modifyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        modifyBtn.setPreferredSize(new Dimension(100, 35));
        modifyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame addDialog = new JFrame();
                addDialog.setSize(300, 200);
                addDialog.setUndecorated(true);
                addDialog.setShape(new RoundRectangle2D.Double(0, 0, 300, 200, 20, 20));
                addDialog.setLocationRelativeTo(orderWindow);
                addDialog.setLayout(new BorderLayout());
                addDialog.setVisible(true);

                JPanel dialogPanel2 = new JPanel(new BorderLayout(10, 10));
                dialogPanel2.setBorder(new EmptyBorder(15, 15, 15, 15));
                dialogPanel2.setBackground(new Color(240, 240, 240));
                addDialog.setContentPane(dialogPanel2);

                JPanel titlePanel2 = new JPanel(new BorderLayout());
                titlePanel2.setBackground(new Color(240, 240, 240));
                JLabel titleLabel2 = new JLabel("修改收货地址", JLabel.CENTER);
                titleLabel2.setFont(new Font("微软雅黑", Font.BOLD, 14));
                titleLabel2.setForeground(new Color(50, 50, 50));
                titlePanel2.add(titleLabel2, BorderLayout.CENTER);
                RoundedButton closeButton2 = new RoundedButton("×", Color.LIGHT_GRAY, Color.GRAY,
                        Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
                closeButton2.setFont(new Font("Arial", Font.BOLD, 14));
                closeButton2.setForeground(Color.WHITE);
                closeButton2.setPreferredSize(new Dimension(20, 20));
                closeButton2.addActionListener(e2 -> addDialog.dispose());
                titlePanel2.add(closeButton2, BorderLayout.EAST);
                dialogPanel2.add(titlePanel2, BorderLayout.NORTH);

                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBackground(Color.WHITE);
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                JPanel addrPanel = createFieldPanel("地址:");
                JTextField addrField = createBorderlessTextField();
                addrPanel.add(addrField);
                contentPanel.add(addrPanel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                contentPanel.add(createSeparator());
                contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                JPanel housePanel = createFieldPanel("门牌号:");
                JTextField houseField = createBorderlessTextField();
                housePanel.add(houseField);
                contentPanel.add(housePanel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                contentPanel.add(createSeparator());
                contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));

                JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                RoundedButton cancelButton = new RoundedButton("取消", Color.LIGHT_GRAY, Color.GRAY,
                        Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
                cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                cancelButton.setPreferredSize(new Dimension(50, 30));
                RoundedButton saveButton = new RoundedButton("保存", Color.LIGHT_GRAY, Color.GRAY,
                        Color.DARK_GRAY, Color.LIGHT_GRAY, 10, 0);
                saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                saveButton.setPreferredSize(new Dimension(50, 30));
                cancelButton.addActionListener(ev -> addDialog.dispose());
                saveButton.addActionListener(ev -> {
                    String addr = addrField.getText().trim();
                    String house = houseField.getText().trim();
                    if (house.isEmpty() || addr.isEmpty()) {
                        JOptionPane.showMessageDialog(addDialog, "请填写必填字段", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    addContentLabel.setText(addr + " " + house);
                    order.address = addr + " " + house;
                    addDialog.dispose();
                });
                buttonPanel2.add(saveButton);
                buttonPanel2.add(cancelButton);
                addDialog.add(contentPanel, BorderLayout.CENTER);
                addDialog.add(buttonPanel2, BorderLayout.SOUTH);
            }
        });
        RoundedButton cancelBtn = new RoundedButton("取消订单", new Color(255, 200, 200),
                new Color(255, 150, 150), new Color(255, 100, 100), Color.RED, 8, 1);
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(orderWindow,
                    "确定要取消订单吗？", "取消订单", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                orderWindow.dispose();
                setGlassPaneVisible(mainMaskPanel, false);
                JOptionPane.showMessageDialog(orderWindow,
                        "订单已取消", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        actionPanel.add(modifyBtn);
        actionPanel.add(cancelBtn);
        contentPanel.add(actionPanel);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        customizeScrollBars(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

    }
    //辅助方法：创建卡片式面板
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 5, 15, 15)
        ));
        return card;
    }
    //辅助方法：创建信息项
    private JPanel createInfoItem(String title, String content, boolean withCopy) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 25));
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setPreferredSize(new Dimension(80, 20));
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        contentLabel.setForeground(new Color(120, 120, 120));
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(contentLabel, BorderLayout.CENTER);
        if (withCopy) {
            RoundedButton copyBtn = new RoundedButton("复制", new Color(240, 240, 240),
                    new Color(220, 220, 220), new Color(200, 200, 200), Color.GRAY, 5, 1);
            copyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            copyBtn.setPreferredSize(new Dimension(50, 20));
            copyBtn.addActionListener(e -> {
                StringSelection stringSelection = new StringSelection(content);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            });
            panel.add(copyBtn, BorderLayout.EAST);
        }
        return panel;
    }
    //辅助方法：创建商品项
    private JPanel createProductItem(CartItem cartItem) {
        Product product = cartItem.getProduct();
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 60));

        JLabel imageLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("图片加载失败");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            imageLabel.setPreferredSize(new Dimension(50, 50));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        imageLabel.setPreferredSize(new Dimension(50, 50));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel specLabel = new JLabel("规格：" + (product.getSpecification() != null ?
                product.getSpecification() : "暂无"));
        specLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        specLabel.setForeground(new Color(150, 150, 150));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(specLabel);
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);
        double price = getDiscountedPrice(product) + product.getPrice() * (cartItem.getQuantity() - 1);
        JLabel priceLabel = new JLabel("¥" + String.format("%.2f", price));
        priceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(100, 100, 100));
        JLabel quantityLabel = new JLabel("×" + cartItem.getQuantity());
        quantityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        quantityLabel.setForeground(new Color(150, 150, 150));
        quantityLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        pricePanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pricePanel.add(quantityLabel);

        panel.add(imageLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(pricePanel, BorderLayout.EAST);

        return panel;
    }
    //辅助方法：创建费用项
    private JPanel createCostItem(String name, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 25));
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(100, 100, 100));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(120, 120, 120));
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }
    //辅助方法：创建订单号码
    String generateOrderNumber() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 15; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }
}

//辅助类：购物车项类
class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product) {
        this.product = product;
        this.quantity = 1;
    }
    
    public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void increaseQuantity() { quantity++; }
    public void decreaseQuantity() {
        if (quantity > 0) { quantity--; }
    }

}

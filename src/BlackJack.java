import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public int getValue() {
            if (value.equals("A")) return 11;
            if (value.equals("J") || value.equals("Q") || value.equals("K")) return 10;
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + value + "-" + type + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random();

    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    int playerWins = 0;
    int dealerWins = 0;
    int ties       = 0;

    int boardWidth  = 700;
    int boardHeight = 650;

    int cardWidth  = 110;
    int cardHeight = 154;

    JFrame frame = new JFrame("Black Jack");

    JPanel gamePanel = new JPanel() {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g.setColor(new Color(34, 85, 50));
                g.fillRect(0, 0, boardWidth, boardHeight);

                g2.setColor(new Color(25, 65, 38));
                g2.fillOval(20, 10, boardWidth - 40, boardHeight - 20);
                g2.setColor(new Color(160, 120, 50));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(20, 10, boardWidth - 40, boardHeight - 20);
                g2.setStroke(new BasicStroke(1));

                int visibleDealerSum = 0;
                for (Card c : dealerHand) visibleDealerSum += c.getValue();

                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(15, 15, 155, 38, 12, 12);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(new Color(255, 200, 50));
                g.drawString("DEALER", 25, 29);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.setColor(new Color(230, 230, 230));
                g.drawString("Points: " + (stayButton.isEnabled() ? visibleDealerSum : reduceDealerAceDisplay()), 25, 47);

                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(15, 310, 155, 38, 12, 12);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(new Color(255, 200, 50));
                g.drawString("YOU", 25, 324);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.setColor(new Color(230, 230, 230));
                g.drawString("Points: " + reducePlayerAceDisplay(), 25, 342);

                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.setColor(new Color(0, 0, 0, 80));
                g.fillRoundRect(24, 64, cardWidth, cardHeight, 6, 6);
                g.drawImage(hiddenCardImg, 20, 60, cardWidth, cardHeight, null);

                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    int x = cardWidth + 25 + (cardWidth + 5) * i;
                    g.setColor(new Color(0, 0, 0, 80));
                    g.fillRoundRect(x + 4, 64, cardWidth, cardHeight, 6, 6);
                    g.drawImage(cardImg, x, 60, cardWidth, cardHeight, null);
                }

                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    int x = 20 + (cardWidth + 5) * i;
                    g.setColor(new Color(0, 0, 0, 80));
                    g.fillRoundRect(x + 4, 364, cardWidth, cardHeight, 6, 6);
                    g.drawImage(cardImg, x, 360, cardWidth, cardHeight, null);
                }

                if (!stayButton.isEnabled()) {

                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();

                    String message = "";
                    Color msgColor;

                    if (playerSum > 21) {
                        message  = "You Lose!";
                        msgColor = new Color(220, 70, 70);
                    } else if (dealerSum > 21) {
                        message  = "You Win!";
                        msgColor = new Color(80, 210, 100);
                    } else if (playerSum == dealerSum) {
                        message  = "Game Tie!";
                        msgColor = new Color(200, 200, 60);
                    } else if (playerSum > dealerSum) {
                        message  = "You Win!";
                        msgColor = new Color(80, 210, 100);
                    } else {
                        message  = "You Lose!";
                        msgColor = new Color(220, 70, 70);
                    }

                    g.setColor(new Color(0, 0, 0, 170));
                    g.fillRoundRect(200, 230, 250, 55, 16, 16);
                    g.setFont(new Font("Arial", Font.BOLD, 30));
                    g.setColor(msgColor);
                    g.drawString(message, 260, 267);

                    restartButton.setEnabled(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    JPanel buttonPanel = new JPanel();

    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton restartButton = new JButton("Restart");

    JLabel winsLabel = new JLabel("Wins: 0");
    JLabel lossLabel = new JLabel("Losses: 0");
    JLabel tiesLabel = new JLabel("Ties: 0");

    BlackJack() {

        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(34, 85, 50));

        frame.add(gamePanel);

        JPanel scoreBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        scoreBar.setBackground(new Color(15, 40, 25));

        JLabel titleLabel = new JLabel("BLACKJACK");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(new Color(255, 200, 50));

        for (JLabel lbl : new JLabel[]{winsLabel, lossLabel, tiesLabel}) {
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(new Color(0, 255, 0));
        }

        scoreBar.add(titleLabel);
        scoreBar.add(winsLabel);
        scoreBar.add(lossLabel);
        scoreBar.add(tiesLabel);
        frame.add(scoreBar, BorderLayout.NORTH);

        buttonPanel.setBackground(new Color(15, 40, 25));

        hitButton.setFocusable(false);
        hitButton.setBackground(new Color(45, 140, 70));
        hitButton.setForeground(Color.WHITE);
        hitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        hitButton.setPreferredSize(new Dimension(120, 38));
        buttonPanel.add(hitButton);

        stayButton.setFocusable(false);
        stayButton.setBackground(new Color(170, 80, 35));
        stayButton.setForeground(Color.WHITE);
        stayButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        stayButton.setPreferredSize(new Dimension(120, 38));
        buttonPanel.add(stayButton);

        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.setBackground(new Color(50, 90, 170));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        restartButton.setPreferredSize(new Dimension(120, 38));
        buttonPanel.add(restartButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Card card = deck.remove(deck.size() - 1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);

                if (reducePlayerAce() > 21) {
                    hitButton.setEnabled(false);
                    stayButton.setEnabled(false);
                    dealerWins++;
                    lossLabel.setText("Losses: " + dealerWins);
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }

                int ps = reducePlayerAce();
                int ds = reduceDealerAce();

                if (ps > 21 || (ds <= 21 && ds > ps)) {
                    dealerWins++;
                    lossLabel.setText("Losses: " + dealerWins);
                } else if (ds > 21 || ps > ds) {
                    playerWins++;
                    winsLabel.setText("Wins: " + playerWins);
                } else {
                    ties++;
                    tiesLabel.setText("Ties: " + ties);
                }

                gamePanel.repaint();
            }
        });

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                startGame();

                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                restartButton.setEnabled(false);

                gamePanel.repaint();
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {

        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size() - 1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }
    }

    public void buildDeck() {

        deck = new ArrayList<Card>();

        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }
    }

    public void shuffleDeck() {

        for (int i = 0; i < deck.size(); i++) {

            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }
    }

    public int reducePlayerAce() {

        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reducePlayerAceDisplay() {
        int sum = playerSum;
        int aces = playerAceCount;
        while (sum > 21 && aces > 0) { sum -= 10; aces--; }
        return sum;
    }

    public int reduceDealerAce() {

        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum      -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    public int reduceDealerAceDisplay() {
        int sum  = dealerSum;
        int aces = dealerAceCount;
        while (sum > 21 && aces > 0) { sum -= 10; aces--; }
        return sum;
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}
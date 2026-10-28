package seedu.address.ui;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final DateTimeFormatter TODAY_HEADER_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final long PAW_PATTERN_SEED = 2103L;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private CustomerListPanel customerListPanel;
    private DogListPanel dogListPanel;
    private ActivityListPanel activityListPanel;
    private TodayActivityListPanel todayActivityListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane customerListPanelPlaceholder;

    @FXML
    private StackPane dogListPanelPlaceholder;

    @FXML
    private StackPane activityListPanelPlaceholder;

    @FXML
    private StackPane todayActivityListPanelPlaceholder;

    @FXML
    private Label todayActivitiesHeader;

    @FXML
    private Pane pawPattern;

    @FXML
    private StackPane resultDisplayPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * JavaFX SDK 17.0.7 still consumes function-key events in TextInputControl.
         * Revisit this workaround when the project upgrades to an SDK version that
         * includes the fix for OpenJDK issue JDK-8131666; remove it only after
         * accelerator tests pass while a TextField or TextArea has focus.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        addPawPattern();

        customerListPanel = new CustomerListPanel(logic.getFilteredCustomerList(), logic.getDogList());
        customerListPanelPlaceholder.getChildren().add(customerListPanel.getRoot());

        dogListPanel = new DogListPanel(logic.getFilteredDogList());
        dogListPanelPlaceholder.getChildren().add(dogListPanel.getRoot());

        activityListPanel = new ActivityListPanel(logic.getFilteredActivityList(), logic.getEnrolmentList(),
                logic::getActivityEnrolmentSummary);
        activityListPanelPlaceholder.getChildren().add(activityListPanel.getRoot());

        todayActivityListPanel = new TodayActivityListPanel(logic.getActivityList());
        todayActivityListPanelPlaceholder.getChildren().add(todayActivityListPanel.getRoot());
        todayActivitiesHeader.setText("Today · " + LocalDate.now().format(TODAY_HEADER_FORMAT));

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /** Adds a subtle repeating paw pattern behind the main window content. */
    private void addPawPattern() {
        pawPattern.widthProperty().addListener((observable, oldWidth, newWidth) ->
                renderPawPattern());
        pawPattern.heightProperty().addListener((observable, oldHeight, newHeight) ->
                renderPawPattern());
        renderPawPattern();
    }

    /** Redraws the paw pattern to cover the currently available background area. */
    private void renderPawPattern() {
        double width = pawPattern.getWidth();
        double height = pawPattern.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        pawPattern.getChildren().clear();
        Color pawColor = Color.web("#90D1CA");
        double horizontalSpacing = 112;
        double verticalSpacing = 96;
        Random random = new Random(PAW_PATTERN_SEED);

        // Start above the visible area and finish beyond the bottom edge so that
        // the pattern remains continuous across the entire resizable background.
        for (int row = -1; row * verticalSpacing < height + 60; row++) {
            double rowJitter = random.nextDouble() * 28 - 14;
            double y = -18 + row * verticalSpacing + rowJitter;
            double rowOffset = row % 2 == 0 ? 0 : horizontalSpacing / 2;
            for (double x = 35 - rowOffset; x < width + 45; x += horizontalSpacing) {
                double xJitter = random.nextDouble() * 36 - 18;
                double yJitter = random.nextDouble() * 24 - 12;
                double rotation = random.nextDouble() * 50 - 25;
                Group paw = createPaw(x + xJitter, y + yJitter, pawColor, rotation);
                pawPattern.getChildren().add(paw);
            }
        }
    }

    /** Creates one small paw mark for the background pattern. */
    private Group createPaw(double x, double y, Color color, double rotation) {
        Ellipse pad = new Ellipse(x, y + 11, 9, 7);
        pad.setFill(color);

        Group paw = new Group(
                pad,
                createToe(x - 8, y + 0.5, 3, color),
                createToe(x - 3, y - 4, 3, color),
                createToe(x + 3, y - 4, 3, color),
                createToe(x + 8, y + 0.5, 3, color));
        paw.setOpacity(0.20);
        paw.setRotate(rotation);
        return paw;
    }

    /** Creates one toe for a paw mark. */
    private Circle createToe(double x, double y, double radius, Color color) {
        Circle toe = new Circle(x, y, radius);
        toe.setFill(color);
        return toe;
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public CustomerListPanel getCustomerListPanel() {
        return customerListPanel;
    }

    public DogListPanel getDogListPanel() {
        return dogListPanel;
    }

    public ActivityListPanel getActivityListPanel() {
        return activityListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }
}

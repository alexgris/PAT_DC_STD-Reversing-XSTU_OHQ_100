package org.project;

import ax.generic.FileProcessing;
import com.pages.*;
import init.settings.SeleniumSetUp;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.By;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Paths;

import static ax.generic.FileProcessing.*;
import static ax.generic.Graphics_Screenshots.*;
import static ax.generic.Waitings.*;
import static org.testng.Assert.assertTrue;


public class Test_Cases extends SeleniumSetUp {

    public Logger logger = Logger.getLogger(Test_Cases.class);

    //global variable that stores the status of "WARNING" (after clicking on "REVERSE" button) popup as displayed/not displayed
    public static boolean popup_Warning = false;


    //global variable for "Item Category"
    public static String itemType;
    public static String itemType_2;
    public static String itemType_3;
    public static String doc_Num;
    public static String doc_Num2;
    public static String doc_NumReversed;
    public static String conf_SystemClient;
    public static String num_StockTransportOrder;





    @BeforeSuite
    public void preTestSettinngs() throws Exception {

        //Delete previously created screenshots from .\src\main\resources\current_images folder
        deleteFilesFromFolder(".\\src\\main\\resources\\current_images");

        //Delete previously created screenshots from .\src\main\resources\error_images folder
        deleteFilesFromFolder(".\\src\\main\\resources\\error_images");

        //Delete previously created log4j files from /log_files_temp folder
        deleteFilesFromFolder(".\\src\\main\\resources\\log_files_temp");

    }


    @BeforeTest
    public void preTestConfigurations() throws Exception {

        //Delete previously created log4j files
        deleteOld_Files(".\\src\\main\\resources\\log_files", "LOG files from 'log_files' folder:");

        //Path to the Log4j logger config file
        PropertyConfigurator.configure(".\\src\\main\\resources\\log4j.properties");

        //Turn on logging level
        //Logger.getRootLogger().setLevel(Level.INFO);

    }

    @Test(priority = 0)
    @Parameters({"target_System_Client", "item_Type_1"})
    public void openDataCollationPage_Testing(String target_System_Client, String item_Type_1) throws Exception {

       driver.manage().window().maximize();

       /*
        //change size of web-browser window
        Dimension d = new Dimension(1600,900);
        driver.manage().window().setSize(d);

        //move web-browser window on the screen
        driver.manage().window().setPosition(new Point(50,50));*/
        conf_SystemClient = target_System_Client;
        System.out.println("Current System: " + conf_SystemClient);
        itemType = item_Type_1;


        //Select correct URL for a particular System/Client
        switch (conf_SystemClient) {

            //Open Data Collation page on OHQ/100
            case "OHQ_100":

                driver.navigate().to("https://ldciohq.wdf.sap.corp:44300/sap/bc/webdynpro/pat/ui_wa_main?WDCONFIGURATIONID=%2fPAT%2fDC_AC_STANDARD&sap-client=100&sap-language=EN#");

                break;

            default:
        }


        waitForExtAjaxIsReadyState(60, "LOGIN or LANDING");


        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class

        //if the application has opened with LOGIN page
        if(landingPage.check_LandingPageOpened() ==  false) {
            landingPage.user_Authentication();
        }


        landingPage.timeToLoad_LandingPage(); //measuring the time needed to load the landing page

        assertTrue(landingPage.check_DocNumberAndDocStatusEmpty()); //confirm that the "Document Number" and "Document Status" fields are empty

        landingPage.check_HeaderDataPanel_Displayed();//if "Header Data" panel is closed then open it
        landingPage.check_ItemDataPanel_Displayed();//if "Item Data" panel is closed then open it

        //click on "Personalize" button on the "Data Collation" toolbar
        landingPage.click_PersonalizeBtn_DataCollationPage();
        Thread.sleep(1000);

        //Select "Set to Default" menu item from "Personalize" dropdown on the "Data Collation" toolbar
        landingPage.click_SetToDefault_DataCollationPage();
        Thread.sleep(1000);

        //click on "Personalize" button on the "Item Data" table
        landingPage.click_PersonalizeBtn();
        Thread.sleep(1000);

        // Adjust width of the columns for better visibility.
        landingPage.set_WidthOfColumns_in_DisplayedColumns_Section();

        //Click on "Save" button on the "Personalization" popup and wait till the landing page gets downloaded again
        landingPage.click_SaveBtn();

        assertTrue(landingPage.isInitialized()); //confirm that page elements are available (i.e. "No data available" icon is visible in the "Add Item" table)
        captureWebElementScreenshot("//body", itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.", "EMPTY DATA COLLATION PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of empty "Data Collation" page after it is opened



////////////////////////////////////---> STN <---//////////////////////////////////////////////////////////////////////////////////////////////////////

        //confirm that the "Add Item" table is empty
        if (landingPage.check_BlankTable_AddItem() != 0) {
            logger.error("The first record in the 'ADD ITEM' table is detected as editable. However, it is expected to be empty after saving PERSONOLIZED settings.");

            //take a screenshot of empty "Data Collation" page after it is opened in case it has errors
            captureErrorScreenshot("//body", itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();
        }

    }


    @Test(dependsOnMethods = {"openDataCollationPage_Testing"})
    public void add_NewItemRow_Testing() throws Exception {

        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class

        //confirm that the "New" button is enabled
        if (landingPage.check_NewBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "NEW" button
            captureErrorScreenshot(landingPage.btn_NewDisabled_OHQ, itemType.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");
            //stop execution and exit
            driver.close();

        } else {

            //click on "New" button
            landingPage.click_NewBtn();

            //check whether "NEW" button changed its status to 'disabled' after clicking on it
            if (landingPage.check_NewBtnEnabledDisabledStatus() != false) {
                logger.warn("'NEW' button did not get DISABLED after clicking action!");
            }
        }


        //confirm that the "Add Item" button is enabled
        if (landingPage.check_AddItemBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "ADD ITEM" button
            captureErrorScreenshot(landingPage.toolbar_AddItem_OHQ, itemType.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {//if "Add Item" button is enabled then ...

            //check whether the "Item Data" table already has automatically added first row
            if (landingPage.check_BlankTable_AddItem() == 0) {

                //click on "Add Item" button
                landingPage.click_AddItemBtn();

                logger.info("'ADD ITEM' button was clicked because the 'ITEM DATA' table has NO editable rows yet.");

                addItemTable.timeToLoad_AddItemEditableRow(); //waiting till the first editable row is loaded in the "Add Item" table

            } else {

                logger.info("The 'ITEM DATA' table already has one editable row which was added automatically.");

            }
        }

    }


    @Test(dependsOnMethods = {"add_NewItemRow_Testing"})
    public void fill_in_NewItemFields_Testing() throws Exception {

        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class

        assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to A

    }

    @Test(dependsOnMethods = {"fill_in_NewItemFields_Testing"}/*, dataProvider="ItemCategories", dataProviderClass= DataproviderClass.class*/)
    @Parameters({"item_Type_1"})
    public void fill_in_ItemCategory_Testing(String item_Type_1) throws Exception {
        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class

        itemType = item_Type_1; //assign Item Category value to a global variable

        // provide value "STN" into "ItemCategory" field
        addItemTable.find_ItemCategoryField_andFillItIn(itemType);

        waitTillWebElementChangesInitialProperty(15, String.format(addItemTable.rightPartColumns_AddItem_Table_OHQ,1,3), "urSTTDRo2", "RIGHT PART of ADD ITEM TABLE");
        Thread.sleep(2000);

    }


    @Test(dependsOnMethods = {"fill_in_ItemCategory_Testing"})
    @Parameters({"input_SupplyPlant", "input_PurchasingGroup", "input_PurchasingOrganization", "input_SlockOfStockTransfer", "cell_Material", "cell_Qty", "cell_Plant", "cell_StorgLocatn", "cell_ValtnType", "cell_HandlingType", "auto_cell_MaterialDescription", "auto_cell_BaseUnitOfMeasure", "cell_Partners", "cell_NetPrice"})
    public void fill_in_RightPartOfTable_Testing(String input_SupplyPlant, String input_PurchasingGroup, String input_PurchasingOrganization, String input_SlockOfStockTransfer,
                                                 String cell_Material, String cell_Qty, String cell_Plant, String cell_StorgLocatn,
                                                 String cell_ValtnType, String cell_HandlingType,
                                                 String auto_cell_MaterialDescription, String auto_cell_BaseUnitOfMeasure, @Optional String cell_Partners, @Optional String cell_NetPrice) throws Exception {


        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class
        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class

        addItemTable.filling_AllRemainingFields(6, itemType, cell_Material, cell_Qty, cell_Plant, cell_StorgLocatn, cell_ValtnType, cell_HandlingType, cell_Partners, cell_NetPrice); //fill in all fields in the right part of the table
        Thread.sleep(2000);

        addItemTable.check_AutocompeteFields(auto_cell_MaterialDescription, auto_cell_BaseUnitOfMeasure); //check that non-editable fields have automatically been assigned values (i.e. not just empty)


        //fill in additional fields (i.e. "Purch. organization", "Purchasing Group", "Supplying Plant", select "Stock Transfer in Stock in Transit" check-box and "SLoc of Stock Transfer" field)
        // in "Detail Data" section -> on "Purchasing" tab


        //Select the table row with created "STN" item and wait/locate the "Detail Data" section on the page
        addItemTable.clickFirstRow_InvokeDetailsSection();

        landingPage.check_DataDetailsPanel_Displayed();//if "Data Details" panel is closed then open it


        //Click on "Purchasing" tab
        if (addItemTable.openPurchasingTab() != false) {

            //click to open "Purchasing" tab if it is not selected by default
            addItemTable.click_toOpenPurchasingTab();
            Thread.sleep(1000);

        }

        //Find "Supplying Plant" field, clean it and enter value
        addItemTable.fillIn_SupplyingPlant(input_SupplyPlant);
        Thread.sleep(1000);

        //Find "Stock Transfer in Stock in Transit" checkbox, select it
        addItemTable.fillIn_StockInTransit();
        Thread.sleep(1000);

        //Find "Purchasing Group" field, clean it and enter value
        addItemTable.fillIn_PurchasingGroup(input_PurchasingGroup);
        Thread.sleep(1000);

        //Find "Purchasing Organization" field, clean it and enter value
        addItemTable.fillIn_PurchasingOrganization(input_PurchasingOrganization);
        Thread.sleep(1000);

        //Find "SLock of Stock Transfer" field, clean it and enter value, then check whether all other remaining fields were auto-filled
        addItemTable.fillIn_SlockOfStockTransfer(input_SlockOfStockTransfer);
        Thread.sleep(1000);

    }


    @Test(dependsOnMethods = {"fill_in_RightPartOfTable_Testing"})
    public void finalize_ProcessingOfSalesOrder_Testing() throws Exception {

        PostProcessing postProcess = new PostProcessing(driver); //Instantiating object of "PostProcessing" class
        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class


        //confirm that the "Check" button is enabled
        if (postProcess.check_CheckBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "CHECK" button
            captureErrorScreenshot(postProcess.toolbar_CheckBtn_OHQ, itemType.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Check" button
            postProcess.click_CheckBtn_StatusCheck();
            //Thread.sleep(2000);

            try{
                //explicitWaitsUntilElementPresent(300, postProcess.complete_DocPAT, "'PAT DOCUMENT IS COMPLETE' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "PAT document is complete"), "visibility", "visible", "'PAT DOCUMENT IS COMPLETE' status message");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STN-Check_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }


            Thread.sleep(1000);


            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to B
        }

        //confirm that the "Release" button is enabled
        if (postProcess.check_ReleaseBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "RELEASE" button
            captureErrorScreenshot(postProcess.toolbar_ReleaseBtn_OHQ, itemType.toUpperCase() + "-Release_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Release" button
            postProcess.click_ReleaseBtn_StatusCheck();
            //Thread.sleep(2000);

            try {
                //explicitWaitsUntilElementPresent(120, postProcess.released_DocPAT, "'PAT DOCUMENT WAS RELEASED' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "PAT document was released"), "visibility", "visible", "'PAT DOCUMENT WAS RELEASED' status message");
            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STN-Release_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(1000);

            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to C
        }


        //confirm that the "Save" button is enabled
        if (postProcess.check_SaveBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "SAVE" button
            captureErrorScreenshot(postProcess.toolbar_SaveBtn_OHQ, itemType.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Save" button
            postProcess.click_SaveBtn_StatusCheck();
            //Thread.sleep(2000);

            try{
                //explicitWaitsUntilElementPresent(120, postProcess.saved_DocPAT, "'PAT DOCUMENT WAS SAVED' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "was saved"), "visibility", "visible", "'PAT DOCUMENT WAS SAVED' status message");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STN-Save_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(1000);

            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to C

            //assign document number to a global variable
            doc_Num = landingPage.retrieve_DocNumber();
        }

        //confirm that the "Process" button is enabled
        if (postProcess.check_ProcessBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "PROCESS" button
            captureErrorScreenshot(postProcess.toolbar_ProcessBtn_OHQ, itemType.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Process" button
            postProcess.click_ProcessBtn_StatusCheck(doc_Num);
            //explicitWaitsUntilElementPresent(120, String.format(postProcess.status_DocumentPosted_OHQ, doc_Num, doc_Num),"PROCESSING OPERATION NOTIFICATION");
            //Thread.sleep(2000);

            try{
                explicitWaitsUntilElementPresent(300, String.format(postProcess.status_DocumentPosted_OHQ, doc_Num, doc_Num), "PROCESSING OPERATION NOTIFICATION");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STN-Process_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(2000);

            //check whether the processing has ended with an error or without
           /* if (postProcess.retrieve_DocProcessingStatus() == false) {

                //if error was displayed then click on "Process" button again
                postProcess.click_ProcessBtn();
                explicitWaitsUntilElementPresent(120, "//span[contains(text(), 'PAT document "+doc_Num+" was posted')]", "PROCESSING SUCCESSFULL NOTIFICATION");
                Thread.sleep(1000);

                explicitWaitsUntilElementPresent(40, "//div[@class='lsHTMLContainer']/div/table[@class='urMatrixLayout urHtmlTableReset']/tbody[@class='urLinStd']/tr[2]/td[2]/div/div/table/tbody/tr/td/descendant::span[contains(text(),'F')]", "DOCUMENT STATUS 'F'");
                Thread.sleep(1000);
                assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F

            }else{
                explicitWaitsUntilElementPresent(40, "//div[@class='lsHTMLContainer']/div/table[@class='urMatrixLayout urHtmlTableReset']/tbody[@class='urLinStd']/tr[2]/td[2]/div/div/table/tbody/tr/td/descendant::span[contains(text(),'F')]", "DOCUMENT STATUS 'F'");
                assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
            }*/

            waitTillElementDetectedByProperty(40, postProcess.status_F_DocProcessed_OHQ, "innerText", "FPosted", "DOCUMENT STATUS 'F'");
            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
            captureWebElementScreenshot("//body", itemType.toUpperCase() + "-Data_Collation_Page_Completed_FullSize.", "COMPLETED DATA COLLATION PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of empty "Data Collation" page after it is opened

        }

    }


    @Test(dependsOnMethods = {"finalize_ProcessingOfSalesOrder_Testing"})
    public void postprocessing_ComparePostedAndCancelledSalesOrder_Testing() throws Exception {

        Locator_DocCompare docCompare = new Locator_DocCompare(driver); //Instantiating object of "Locator_DocCompare" class
        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class

        //confirm that the "Locator" button is enabled
        if (docCompare.check_LocatorBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "LOCATOR" button
            captureErrorScreenshot(docCompare.toolbar_LocatorBtn_OHQ, itemType.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "LOCATOR" button
            docCompare.click_LocatorBtn_StatusCheck();

            //wait till "Locator" panel gets displayed
            explicitWaitsUntilElementPresent(40, docCompare.panel_Locator_OHQ, "LOCATOR PANEL");
            Thread.sleep(1000);

            //Click on "Personalize" button in "Locator" panel
            docCompare.click_PersonalizeBtn_LocatorPanel();
            Thread.sleep(1000);

            // Adjust width of the columns for better visibility.
            //Check that check-marks a set for the 6 columns "Item status", "Transfer posting", "Item Number", "Sequence Number of the Item", "Posting Sequence" and "Item Category"
            docCompare.set_WidthOfColumns_in_DisplayedColumns_Section_Persnlztn_Locator();

            //Click on "Save" button on the "Personalization" popup (for "Locator" panel) and wait till the landing page gets downloaded again
            landingPage.click_SaveBtn();
            Thread.sleep(1000);

            //providing Document number into Search Criteria field
            docCompare.fillout_SearchCriteria(doc_Num);
            Thread.sleep(1000);

            //click on "Search" button and wait till documents are displayed in the lower table
            docCompare.click_SearchBtn(doc_Num);
            Thread.sleep(1000);

            //check the status of the found document
            docCompare.check_StatusOfFoundDocument();
            Thread.sleep(1000);

            //check if DocStatus correctly correlates with DocNum in the "Results" table
            docCompare.correlate_DocStatusAndDocNum(doc_Num);
            Thread.sleep(1000);

            //take a screenshot of the "Locator" panel
            captureWebElementScreenshot(docCompare.wholePanel_Locator_OHQ, itemType.toUpperCase() + "-Locator_Panel_DocStatus.", "LOCATOR PANEL WITH DOCUMENT STATUS", ".\\src\\main\\resources\\current_images\\");
            Thread.sleep(2000);

            //confirm that the "Locator" button is enabled
            if (docCompare.check_LocatorBtnEnabledDisabledStatus() == false) {
                logger.error("LOCATOR button is DISABLED. It is expected to be ENABLED to continue!");

                //stop execution and exit
                driver.close();
            } else {
                docCompare.click_LocatorBtn();//click on "LOCATOR" button to close "Locator" panel

            }

            Thread.sleep(3000);

        }

    }


    @Test(dependsOnMethods = {"postprocessing_ComparePostedAndCancelledSalesOrder_Testing"})
    public void postprocessing_Read_StockTransportOrderNumber_Testing() throws Exception {

        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class


        landingPage.check_DocumentFlowPanel_Displayed();//if "Document Flow" panel is closed then open it
        Thread.sleep(1000);

       // landingPage.click_FirstRow_in_DocumentFlowTable();//find the first row in "Document Flow" table and click on the arrow to open lower-level rows
       // Thread.sleep(1000);

        num_StockTransportOrder = landingPage.read_StockTransportOrderNumber_from_DocumentFlowTable();//find the second rows in "Document Flow" table and read the number of  Stock Transport Order number and save it in global variable

    }

////////////////////////////////////---> STU <---//////////////////////////////////////////////////////////////////////////////////////////////////////


    @Test(dependsOnMethods = {"postprocessing_Read_StockTransportOrderNumber_Testing"})
    @Parameters({"item_Type_2"})
    public void add_NewItemRowForNewDocument_Testing(String item_Type_2) throws Exception {

        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class

        itemType_2 = item_Type_2;

        //confirm that the "New" button is enabled
        if (landingPage.check_NewBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "NEW" button
            captureErrorScreenshot(landingPage.btn_NewDisabled_OHQ, itemType_2.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {

            //click on "New" button
            landingPage.click_NewBtn();

            //check whether "NEW" button changed its status to 'disabled' after clicking on it
            if (landingPage.check_NewBtnEnabledDisabledStatus() != false) {
                logger.warn("'NEW' button did not get DISABLED after clicking action!");
            }

            captureWebElementScreenshot("//body", itemType_2.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.", "STU EMPTY PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of empty "Data Collation" page after it is opened

        }


        //confirm that the "Add Item" button is enabled
        if (landingPage.check_AddItemBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "ADD ITEM" button
            captureErrorScreenshot(landingPage.toolbar_AddItem_OHQ, itemType_2.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {//if "Add Item" button is enabled then ...

            //check whether the "Item Data" table already has automatically added first row
            if (landingPage.check_BlankTable_AddItem() == 0) {

                //click on "Add Item" button
                landingPage.click_AddItemBtn();

                logger.info("'ADD ITEM' button was clicked because the 'ITEM DATA' table has NO editable rows yet.");

                addItemTable.timeToLoad_AddItemEditableRow(); //waiting till the first editable row is loaded in the "Add Item" table

            } else {

                logger.info("The 'ITEM DATA' table already has one editable row which was added automatically.");

            }
        }

    }


    @Test(dependsOnMethods = {"add_NewItemRowForNewDocument_Testing"})
    public void fill_in_NewItemFieldsForNewDocument_Testing() throws Exception {

        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class

        assertTrue(addItemTable.isInitialized()); //checking whether the first editable row is displayed or not in the "Add Item" table
        Thread.sleep(2000);
        assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to A

    }


    @Test(dependsOnMethods = {"fill_in_NewItemFieldsForNewDocument_Testing"}/*, dataProvider="ItemCategories", dataProviderClass= DataproviderClass.class*/)
    public void fill_in_ItemCategoryForNewDocument_Testing() throws Exception {
        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class

        // provide value "STU" into "ItemCategory" field
        addItemTable.find_ItemCategoryField_andFillItIn(itemType_2);

        //wait till "Reference Document" input gets enabled
        waitTillWebElementChangesInitialProperty(15, addItemTable.enabledInput_ReferenceDoc_OHQ, "urSTTDRo2", "RIGHT PART of ADD ITEM TABLE");

        Thread.sleep(2000);
    }




    @Test(dependsOnMethods = {"fill_in_ItemCategoryForNewDocument_Testing"})
    @Parameters({"cell_Material", "cell_Qty", "cell_Plant", "cell_StorgLocatn", "cell_ValtnType", "cell_HandlingType", "auto_cell_MaterialDescription", "auto_cell_BaseUnitOfMeasure"})
    public void fill_in_RightPartOfTableForOtherDocument_Testing(String cell_Material, String cell_Qty, String cell_Plant, String cell_StorgLocatn,
                                                 String cell_ValtnType, String cell_HandlingType,
                                                 String auto_cell_MaterialDescription, String auto_cell_BaseUnitOfMeasure) throws Exception {


        AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "addItemTable" class
        PurchaseOrderPage pagePurchOrder = new PurchaseOrderPage(driver); //Instantiating object of "PurchaseOrderPage" class

        addItemTable.filling_AllRemainingFieldsForOtherDocuments(num_StockTransportOrder); //fill in "Reference Document" field in the right part of the table

        explicitWaitsUntilElementPresent(120, String.format(pagePurchOrder.status_POimport_OHQ, num_StockTransportOrder), "PROCESSING IMPORT FROM PURCHASE ORDER '"+num_StockTransportOrder+"'");

        Thread.sleep(1000);

        String cell_ReferenceItem = "";
        String auto_cell_Material = "";

        addItemTable.check_AutocompeteFieldsForOtherDocuments(8, cell_ReferenceItem, auto_cell_Material, auto_cell_MaterialDescription, cell_Qty, auto_cell_BaseUnitOfMeasure, cell_Plant, cell_StorgLocatn, cell_ValtnType, cell_HandlingType); //check that non-editable fields have automatically been assigned values (i.e. not just empty)

    }


    @Test(dependsOnMethods = {"fill_in_RightPartOfTableForOtherDocument_Testing"})
    public void finalize_ProcessingOfSalesOrderForOtherDocument_Testing() throws Exception {

        PostProcessing postProcess = new PostProcessing(driver); //Instantiating object of "PostProcessing" class
        LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
        PurchaseOrderPage pagePurchOrder = new PurchaseOrderPage(driver); //Instantiating object of "PurchaseOrderPage" class



        //confirm that the "Check" button is enabled
        if (postProcess.check_CheckBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "CHECK" button
            captureErrorScreenshot(postProcess.toolbar_CheckBtn_OHQ, itemType_2.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Check" button
            postProcess.click_CheckBtn_StatusCheck();
            //Thread.sleep(2000);

            try{
                //explicitWaitsUntilElementPresent(300, postProcess.complete_DocPAT, "'PAT DOCUMENT IS COMPLETE' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "PAT document is complete"), "visibility", "visible", "'PAT DOCUMENT IS COMPLETE' status message");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STU-Check_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }


            Thread.sleep(1000);


            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to B
        }

        //confirm that the "Release" button is enabled
        if (postProcess.check_ReleaseBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "RELEASE" button
            captureErrorScreenshot(postProcess.toolbar_ReleaseBtn_OHQ, itemType_2.toUpperCase() + "-Release_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Release" button
            postProcess.click_ReleaseBtn_StatusCheck();
            //Thread.sleep(2000);

            try {
                //explicitWaitsUntilElementPresent(120, postProcess.released_DocPAT, "'PAT DOCUMENT WAS RELEASED' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "PAT document was released"), "visibility", "visible", "'PAT DOCUMENT WAS RELEASED' status message");
            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STU-Release_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(1000);

            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to C
        }


        //confirm that the "Save" button is enabled
        if (postProcess.check_SaveBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "SAVE" button
            captureErrorScreenshot(postProcess.toolbar_SaveBtn_OHQ, itemType_2.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Save" button
            postProcess.click_SaveBtn_StatusCheck();
            //Thread.sleep(2000);

            try{
                //explicitWaitsUntilElementPresent(120, postProcess.saved_DocPAT, "'PAT DOCUMENT WAS SAVED' status message");
                waitTillElementDetectedByStyle(300, String.format(postProcess.allMessages_DocPAT, "was saved"), "visibility", "visible", "'PAT DOCUMENT WAS SAVED' status message");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STU-Save_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(1000);

            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is empty and "Document Status" fields is set to C

            //assign document number to a global variable
            doc_Num2 = landingPage.retrieve_DocNumber();
        }

        //confirm that the "Process" button is enabled
        if (postProcess.check_ProcessBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "PROCESS" button
            captureErrorScreenshot(postProcess.toolbar_ProcessBtn_OHQ, itemType_2.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "Process" button
            postProcess.click_ProcessBtn_StatusCheck(doc_Num2);
            //explicitWaitsUntilElementPresent(120, String.format(pagePurchOrder.status_ImportedDoc_OHQ, doc_Num2, doc_Num2), "PROCESSING OPERATION NOTIFICATION");
            //Thread.sleep(2000);

            try{
                explicitWaitsUntilElementPresent(300, String.format(postProcess.status_DocumentPosted_OHQ, doc_Num2, doc_Num2), "PROCESSING OPERATION NOTIFICATION");

            }catch (Exception e){
                logger.error("Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:" + e.getMessage());

                //take a screenshot of the main page
                captureErrorScreenshot("//body", "STU-Process_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

            }

            Thread.sleep(2000);

            //check whether the processing has ended with an error or without
           /* if (postProcess.retrieve_DocProcessingStatus() == false) {

                //if error was displayed then click on "Process" button again
                postProcess.click_ProcessBtn();
                explicitWaitsUntilElementPresent(120, "//*[contains(text(), 'PAT document "+doc_Num+" was posted')]", "PROCESSING SUCCESSFULL NOTIFICATION");
                Thread.sleep(1000);

                explicitWaitsUntilElementPresent(40, "//div[@ct='SC'][contains (@class, 'lsScrollContainer--nonescrolling')]/div/table/tbody/tr[2]/td[2]/descendant::td[contains(@class, 'lsContainerCell urLayoutRPad')]/descendant::span[contains(text()='F')]", "DOCUMENT STATUS 'F'");
                Thread.sleep(1000);
                assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F

            }else{
                explicitWaitsUntilElementPresent(40, "//div[@ct='SC'][contains (@class, 'lsScrollContainer--nonescrolling')]/div/table/tbody/tr[2]/td[2]/descendant::td[contains(@class, 'lsContainerCell urLayoutRPad')]/descendant::span[contains(text()='F')]", "DOCUMENT STATUS 'F'");
                assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
            }*/

            assertTrue(landingPage.check_DocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
            captureWebElementScreenshot("//body", itemType_2.toUpperCase() + "-Data_Collation_Page_PostProcessing_Completed_FullSize.", "COMPLETED POSTPROCESSING DATA COLLATION PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of empty "Data Collation" page after it is opened

        }

    }


    @Test(dependsOnMethods = {"finalize_ProcessingOfSalesOrderForOtherDocument_Testing"})
    public void postprocessing_ComparePostedAndCancelledSalesOrderForOtherDocument_Testing() throws Exception {

        Locator_DocCompare docCompare = new Locator_DocCompare(driver); //Instantiating object of "Locator_DocCompare" class

        //confirm that the "Locator" button is enabled
        if (docCompare.check_LocatorBtnEnabledDisabledStatus() == false) {

            //take a screenshot of the toolbar with "LOCATOR" button
            captureErrorScreenshot(docCompare.toolbar_LocatorBtn_OHQ, itemType_2.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

            //stop execution and exit
            driver.close();

        } else {
            //click on "LOCATOR" button
            docCompare.click_LocatorBtn_StatusCheck();

            //wait till "Locator" panel gets displayed
            explicitWaitsUntilElementPresent(40, docCompare.panel_Locator_OHQ, "LOCATOR PANEL");
            Thread.sleep(1000);

            //providing Document number into Search Criteria field
            docCompare.fillout_SearchCriteria(doc_Num2);
            Thread.sleep(1000);

            //click on "Search" button and wait till documents are displayed in the lower table
            docCompare.click_SearchBtn(doc_Num2);
            Thread.sleep(1000);

            //check the status of the found document
            docCompare.check_StatusOfFoundDocument();
            Thread.sleep(1000);

            //check if DocStatus correctly correlates with DocNum in the "Results" table
            docCompare.correlate_DocStatusAndDocNum(doc_Num2);
            Thread.sleep(1000);

            //take a screenshot of the "Locator" panel
            captureWebElementScreenshot(docCompare.wholePanel_Locator_OHQ, itemType_2.toUpperCase() + "-Locator_Panel_Postprocessing_DocStatus.", "LOCATOR POSTPROCESSING PANEL WITH DOCUMENT STATUS", ".\\src\\main\\resources\\current_images\\");

            Thread.sleep(2000);

            //confirm that the "Locator" button is enabled
            if (docCompare.check_LocatorBtnEnabledDisabledStatus() == false) {
                logger.error("LOCATOR button is DISABLED. It is expected to be ENABLED to continue!");

                //stop execution and exit
                driver.close();
            } else {
                docCompare.click_LocatorBtn();//click on "LOCATOR" button to close "Locator" panel

                Thread.sleep(3000);

                //wait till "Locator" panel escapes
                //waitTillWebElementEscapes(15, "//span[contains (text(), 'Locator')]/ancestor::td[@class='urSpTPTD'][1]", "LOCATOR PANEL");
                //waitTillDescendentElementsLessThanExpected(3, "//div[contains (@id, 'sapwd_main_window_root_')][contains (@ct, 'PAGE')]/div[1]/table/tbody[contains (@class, 'urLinStd')]/tr[3]/td[contains (@ct, 'MLC')]/descendant::*", "LOCATOR PANEL CLOSING", 1);
                // waitTillElementDetectedByStyle(5, "//div[contains (@id, 'sapwd_main_window_root_')][contains (@ct, 'PAGE')]/div[1]/table/tbody[contains (@class, 'urLinStd')]/tr[3]/td[contains (@ct, 'MLC')]", "width", "100%", "LOCATOR PANEL CLOSING");
                //waitTillWebElementsDisapper(3, "//div[contains (@id, 'sapwd_main_window_root_')][contains (@ct, 'PAGE')]/div[1]/table/tbody[contains (@class, 'urLinStd')]/tr[3]/td[contains (@ct, 'MLC')][contains (@class, 'lsContainerCell lsContainerCellVAlign--top urLayoutPadless')]/descendant::td", "LOCATOR PANEL CLOSING");

            }

        }
    }

        ////////////////////////////////////---> XSTU <---//////////////////////////////////////////////////////////////////////////////////////////////////////

        @Test(dependsOnMethods = {"postprocessing_ComparePostedAndCancelledSalesOrderForOtherDocument_Testing"})
        @Parameters({"item_Type_3"})
        public void postprocessing_ReverseSalesOrder_Testing(String item_Type_3) throws Exception {

            PostProcessing postProcess = new PostProcessing(driver); //Instantiating object of "PostProcessing" class
            LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
            AddItemTable addItemTable = new AddItemTable(driver); //Instantiating object of "AddItemTable" class

            //assigning item type XSTU of reversed document to a global variable
            itemType_3 = item_Type_3;


            //confirm that the "Reverse" button is enabled
            if (postProcess.check_ReverseBtnEnabledDisabledStatus() == false) {

                //take a screenshot of the toolbar with "REVERSE" button
                captureErrorScreenshot(postProcess.toolbar_ReverseBtn_OHQ, itemType_3.toUpperCase() + "-Reverse_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

                //stop execution and exit
                driver.close();

            }
            else {
                //click on "Reverse" button
                postProcess.click_ReverseBtn_StatusCheck_RoundTwo();
                Thread.sleep(2000);


                //Select the parent web element for "Warning" popup depending on the system and client

                    driver.switchTo().frame(driver.findElement(By.xpath(landingPage.iFrame_1_OHQ)));

                waitTillPredefinedDescendentElements(20, postProcess.popup_ReverseWarning_OHQ, "WARNING POPUP ON REVERSE ACTION", 4);
                Thread.sleep(1000);

                    //check if "WARNING" popup is really displayed
                   popup_Warning = addItemTable.check_IfWarningOnReversePopupDisplayed_OHQ();

                    //if "Warning" popup is displayed
                    if (popup_Warning == true) {

                        //close the "Warning" popup and wait till it gets escaped
                        landingPage.close_WarningPOPUP();

                        driver.switchTo().defaultContent();

                        waitForExtAjaxIsReadyState(40, "WARNING POPUP");

                        Thread.sleep(2000);
                    }


                //Check the status of reversed document, as well as "Item Category", "Reference Number" and "Reference Item"
                landingPage.check_ReversedDocDetails(doc_Num2, itemType_3.toUpperCase());

                //assign document number to a global variable
                doc_NumReversed = landingPage.retrieve_ReversedDocNumber();

                //Make the screenshot of the page with reversed document
                captureWebElementScreenshot("//body", itemType_3.toUpperCase() + "-Reversed_Document_Page_Completed_FullSize.", "REVERSED DOCUMENT PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of a "Data Collation" page after the document was reversed


            }
        }


        @Test(dependsOnMethods = {"postprocessing_ReverseSalesOrder_Testing"})
        public void postprocessing_RePostingCancelledSalesOrder_Testing() throws Exception {

            PostProcessing postProcess = new PostProcessing(driver); //Instantiating object of "PostProcessing" class
            LandingPage landingPage = new LandingPage(driver); //Instantiating object of "LandingPage" class
            Locator_DocCompare docCompare = new Locator_DocCompare(driver); //Instantiating object of "Locator_DocCompare" class
            Locator_DocSearch locatorDocSearch = new Locator_DocSearch(driver); //Instantiating object of "Locator_DocSearch" class

            //Check the status of reversed document, as well as "Item Category", "Reference Number" and "Reference Item"
            if (landingPage.check_ReversedDocDetails(doc_Num2, itemType_3.toUpperCase()) == true) {

                //Process again the reversed document
                //confirm that the "Process" button is enabled
                if (postProcess.check_ProcessBtnEnabledDisabledStatus() == false) {

                    //take a screenshot of the toolbar with "PROCESS" button
                    captureErrorScreenshot(postProcess.toolbar_ProcessBtn_OHQ, itemType_3.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.", ".\\src\\main\\resources\\error_images\\");

                    //stop execution and exit
                    driver.close();

                }

                else {
                    //click on "Process" button
                    postProcess.click_ProcessBtn_StatusCheck_AfterCancellation();

                    //explicitWaitsUntilElementPresent(120, String.format(postProcess.status_DocumentPosted_OHQ, doc_NumReversed, doc_NumReversed), "PROCESSING OPERATION NOTIFICATION");

                    try{
                        explicitWaitsUntilElementPresent(300, String.format(postProcess.status_DocumentPosted_OHQ, doc_NumReversed, doc_NumReversed), "PROCESSING OPERATION NOTIFICATION");

                    }catch (Exception e){
                        logger.error("Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:" + e.getMessage());

                        //take a screenshot of the main page
                        captureErrorScreenshot("//body", "XSTU-Process_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

                    }

                    Thread.sleep(2000);

                    explicitWaitsUntilElementPresent(40, postProcess.status_F_DocProcessed_OHQ, "DOCUMENT STATUS 'F'");
                    Thread.sleep(1000);

                    assertTrue(landingPage.check_ReversedDocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
                    captureWebElementScreenshot("//body", itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize_After_2nd_Attempt.", "REPROCESSED DOCUMENT PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of the processed "Data Collation" page after it was reversed

                }

            } else {//if main page was not auto-refreshed after "Reverse" action

                //open "Locator" panel
                docCompare.click_LocatorBtn_StatusCheck();

                //wait till "Locator" panel gets displayed
                explicitWaitsUntilElementPresent(40, docCompare.panel_Locator_OHQ, "LOCATOR PANEL");
                Thread.sleep(1000);

                //providing Document number (increased by 1) into Search Criteria field
                docCompare.fillout_SearchCriteria_FailedAutoRefresh(doc_Num2);
                Thread.sleep(1000);

                //click on "Search" button and wait till documents are displayed in the lower table
                docCompare.click_SearchBtn(doc_NumReversed);
                Thread.sleep(1000);

                //check that "current STU number" + 1 found in search results
                //check if the search status is equal to "None entries for the selection found"
                if (locatorDocSearch.check_IfElementIsPresent(locatorDocSearch.status_NoneEntriesFound)) {
                    logger.warn("NONE ENTRIES FOR THE 'REVERSED' DOCUMENT ARE FOUND after clicking on 'SEARCH' button on LOCATOR panel.");

                    //stop execution and exit
                    driver.close();
                } else {
                    locatorDocSearch.click_OnFirstRowWithFoundDoc(doc_Num2);//clicking on the row with found document

                    //increment current document number by 1 to coinside with the Reversed Doc Number
                    Integer doc_reversed = Integer.valueOf(doc_Num2);
                    String doc_NumRevrsd = Integer.toString(doc_reversed + 1);


                    //wait till the "Process" button gets enabled
                    waitTillElementDetectedByProperty(15, landingPage.enabledButton_Process_OHQ, "innerText", doc_NumRevrsd, "UPDATED 'DATA COLLATION' page after clicking on the result in LOCATOR panel");
                    Thread.sleep(1000);

                    //Scroll back to the top of the page
                    landingPage.mainPage_scrollToTheTop();
                    Thread.sleep(1000);

                    docCompare.click_LocatorBtn();//click on "LOCATOR" button to close "Locator" panel
                    Thread.sleep(2000);


                    //click on "Process" button
                    postProcess.click_ProcessBtn_StatusCheck_AfterCancellation();

                    //explicitWaitsUntilElementPresent(120, String.format(postProcess.status_DocumentPosted_OHQ, doc_NumRevrsd, doc_NumRevrsd), "PROCESSING OPERATION NOTIFICATION");

                    try{
                        explicitWaitsUntilElementPresent(300, String.format(postProcess.status_DocumentPosted_OHQ, doc_NumRevrsd, doc_NumRevrsd), "PROCESSING OPERATION NOTIFICATION");

                    }catch (Exception e){
                        logger.error("Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:" + e.getMessage());

                        //take a screenshot of the main page
                        captureErrorScreenshot("//body", "XSTU-Process_Btn_Status_Message_Failure.", ".\\src\\main\\resources\\error_images\\");

                    }

                    Thread.sleep(2000);

                    explicitWaitsUntilElementPresent(40, postProcess.status_F_DocProcessed_OHQ, "DOCUMENT STATUS 'F'");

                    Thread.sleep(1000);

                    assertTrue(landingPage.check_ReversedDocNumberAndDocStatus()); //confirm that the "Document Number" is displayed and "Document Status" fields is set to F
                    captureWebElementScreenshot("//body", itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize.", "REPROCESSED DOCUMENT PAGE", ".\\src\\main\\resources\\current_images\\"); //take a screenshot of the processed "Data Collation" page after it was reversed

                    Integer docNum = Integer.valueOf(Test_Cases.doc_NumReversed);

                    //deminish reversed Doc number by 2 to set it to STN (1st) doc number
                    Test_Cases.doc_Num = Integer.toString(docNum - 2);
                    System.out.println("DocNUM of the very 1st document (STN): " + Test_Cases.doc_Num);

                    //deminish reversed Doc number by 1 to set it STU (2nd) doc number
                    Test_Cases.doc_Num2 = Integer.toString(docNum - 1);
                    System.out.println("DocNUM of the 2nd document (STU): " + Test_Cases.doc_Num2);

                    Thread.sleep(8000);

                }

            }

        }



    @AfterTest
    @Parameters({"target_System"})
    public void insertLinksIntoLogFile(String target_System) {

        FileProcessing fileProcessing = new FileProcessing(); //Instantiating object of "FileProcessing" class


        //create a separate log file for every Item Category
        try {
            fileProcessing.renameLogHTMLfile(itemType_3);
        } catch (IOException e) {
            logger.error("Unable to run the 'fileProcessing.renameLogHTMLfile(itemType)' method: " + e.getMessage());
        }

        //inserting "DATA COLLATION" empty page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\"+itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.png")) {
                String tdLineBeforeChange = "EMPTY DATA COLLATION PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\"+itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.png").toRealPath().toUri().toURL() + ">"+ itemType.toUpperCase() + "_EMPTY_DATA_COLLATION_PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for Empty DATA COLLATION page: " + e.getMessage());
        }


        //inserting "DATA COLLATION" empty page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\"+itemType_2.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.png")) {
                String tdLineBeforeChange = "STU EMPTY PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\"+itemType_2.toUpperCase()+"-Data_Collation_Page_Empty_FullSize.png").toRealPath().toUri().toURL() + ">"+ itemType_2.toUpperCase() + "_EMPTY_PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for Empty DATA COLLATION page: " + e.getMessage());
        }


        //inserting "DATA COLLATION with ERROR(s)" empty page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize_Error.png")) {
                String tdLineBeforeChange = "One or more records detected in the 'ADD ITEM' table. However it is expected to be empty.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-Data_Collation_Page_Empty_FullSize_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_One_or_more_records_detected_in_the_'ADD_ITEM'_table.</a>";
                String endText = "However it is expected to be empty.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for Empty DATA COLLATION page with ERROR: " + e.getMessage());
        }

        //inserting "Button 'NEW' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'NEW' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'NEW'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'NEW' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'NEW' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\"+itemType_2.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'NEW' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\"+itemType_2.toUpperCase()+"-New_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'NEW'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'NEW' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'ADD ITEM' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'ADD ITEM' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\"+itemType.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'ADD_ITEM'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'ADD ITEM' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'ADD ITEM' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\"+itemType_2.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'ADD ITEM' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\"+itemType_2.toUpperCase()+"-AddItem_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'ADD_ITEM'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'ADD ITEM' button is disabled or not found ERROR: " + e.getMessage());
        }



        //inserting "Button 'CHECK' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'CHECK' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'CHECK'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'CHECK' button is disabled or not found ERROR: " + e.getMessage());
        }

        //inserting "Button 'CHECK' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'CHECK' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Check_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'CHECK'</a>";
                String endText = "is in DISABLED status.";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'CHECK' button is disabled or not found ERROR: " + e.getMessage());
        }

        //inserting "Button 'RELEASE' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Release_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'RELEASE' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "Release_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'RELEASE'</a>";
                String endText = "is in DISABLED status.";

                //inserting "RELEASE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'RELEASE' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'RELEASE' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Release_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'RELEASE' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "Release_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'RELEASE'</a>";
                String endText = "is in DISABLED status.";

                //inserting "RELEASE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'RELEASE' button is disabled or not found ERROR: " + e.getMessage());
        }

        //inserting "Button 'SAVE' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'SAVE' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'SAVE'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'SAVE' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'SAVE' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'SAVE' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Save_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'SAVE'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'SAVE' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'PROCESS' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'PROCESS'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'PROCESS' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'PROCESS'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_3.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'PROCESS' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_3.toUpperCase() + "-Process_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_3.toUpperCase() + "_Button_'PROCESS'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'REVERSE' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_3.toUpperCase() + "-Reverse_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'REVERSE' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_3.toUpperCase() + "-Reverse_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_3.toUpperCase() + "_Button_'REVERSE'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'REVERSE' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "DATA COLLATION" completed page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType.toUpperCase() + "-Data_Collation_Page_Completed_FullSize.png")) {
                String tdLineBeforeChange = "COMPLETED DATA COLLATION PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\STN-Data_Collation_Page_Completed_FullSize.png").toRealPath().toUri().toURL() + ">"+ itemType.toUpperCase() + "_COMPLETED-DATA-COLLATION-PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "DATA COLLATION" completed page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for Completed DATA COLLATION page: " + e.getMessage());
        }


        //inserting "DATA COLLATION" postprocessing completed page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType_2.toUpperCase() + "-Data_Collation_Page_PostProcessing_Completed_FullSize.png")) {
                String tdLineBeforeChange = "COMPLETED POSTPROCESSING DATA COLLATION PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\" + itemType_2.toUpperCase() + "-Data_Collation_Page_PostProcessing_Completed_FullSize.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_COMPLETED-POSTPROCESSING-DATA-COLLATION-PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "DATA COLLATION" completed page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for Completed Postprocessing DATA COLLATION page: " + e.getMessage());
        }

        //inserting "REVERSED DOCUMENT PAGE" completed page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-Reversed_Document_Page_Completed_FullSize.png")) {
                String tdLineBeforeChange = "REVERSED DOCUMENT PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-Reversed_Document_Page_Completed_FullSize.png").toRealPath().toUri().toURL() + ">" + itemType_3.toUpperCase() + "_REVERSED_DOCUMENT_PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "REVERSED DOCUMENT PAGE" completed page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for REVERSED DOCUMENT page: " + e.getMessage());
        }


        //inserting "PROCESSED REVERSED DOCUMENT PAGE" completed page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize_After_2nd_Attempt.png")) {
                String tdLineBeforeChange = "REPROCESSED DOCUMENT PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize_After_2nd_Attempt.png").toRealPath().toUri().toURL() + ">" + itemType_3.toUpperCase() + "_REPROCESSED_DOCUMENT_PAGE_AFTER_2nd_ATTEMPT</a>";
                String endText = "window screenshot was made.";

                //inserting "REVERSED DOCUMENT PAGE" completed page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for REPROCESSED DOCUMENT PAGE page: " + e.getMessage());
        }



        //inserting "PROCESSED REVERSED DOCUMENT PAGE" completed page screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize.png")) {
                String tdLineBeforeChange = "REPROCESSED DOCUMENT PAGE window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\" + itemType_3.toUpperCase() + "-ReProcessed_Document_Page_FullSize.png").toRealPath().toUri().toURL() + ">" + itemType_3.toUpperCase() + "_REPROCESSED_DOCUMENT_PAGE</a>";
                String endText = "window screenshot was made.";

                //inserting "REVERSED DOCUMENT PAGE" completed page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);

                //logger.info("URL Link for Empty DATA COLLATION page was successfully added to the 'log4j-application.html' file");
            }

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for REPROCESSED DOCUMENT PAGE page: " + e.getMessage());
        }


        //inserting "Button 'LOCATOR' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'LOCATOR' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType.toUpperCase() + "_Button_'LOCATOR'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'LOCATOR' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "Button 'LOCATOR' is disabled or not found!" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.png")) {
                String tdLineBeforeChange = "Button 'LOCATOR' is in DISABLED status.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\" + itemType_2.toUpperCase() + "-Locator_Btn_Disabled_NotFound_Error.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_Button_'LOCATOR'</a>";
                String endText = "is in DISABLED status.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'LOCATOR' button is disabled or not found ERROR: " + e.getMessage());
        }


        //inserting "LOCATOR" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\STN-Locator_Panel_DocStatus.png")) {
                String tdLineBeforeChange = "LOCATOR PANEL WITH DOCUMENT STATUS window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\STN-Locator_Panel_DocStatus.png").toRealPath().toUri().toURL() + ">STN_LOCATOR PANEL WITH DOCUMENT STATUS'</a>";
                String endText = "window screenshot was made.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for LOCATOR PANEL WITH DOCUMENT STATUS page: " + e.getMessage());
        }


        //inserting "LOCATOR" postprocessing screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\current_images\\" + itemType_2.toUpperCase() + "-Locator_Panel_Postprocessing_DocStatus.png")) {
                String tdLineBeforeChange = "LOCATOR POSTPROCESSING PANEL WITH DOCUMENT STATUS window screenshot was made.";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\current_images\\" + itemType_2.toUpperCase() + "-Locator_Panel_Postprocessing_DocStatus.png").toRealPath().toUri().toURL() + ">" + itemType_2.toUpperCase() + "_LOCATOR-POSTPROCESSING-PANEL-WITH-DOCUMENT-STATUS'</a>";
                String endText = "window screenshot was made.";

                //inserting "SAVE" button screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }
            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for LOCATOR POSTPROCESSING PANEL WITH DOCUMENT STATUS page: " + e.getMessage());
        }

        //inserting "Button 'CHECK' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STN-Check_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STN-Check_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'CHECK' button status message FAILURE: " + e.getMessage());
        }



        //inserting "Button 'RELEASE' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STN-Release_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STN-Release_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'RELEASE' button status message FAILURE: " + e.getMessage());
        }


        //inserting "Button 'SAVE' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STN-Save_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STN-Save_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'SAVE' button status message FAILURE: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STN-Process_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STN-Process_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button status message FAILURE: " + e.getMessage());
        }

        //inserting "Button 'CHECK' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STU-Check_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STU-Check_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT IS COMPLETE' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'CHECK' button status message FAILURE: " + e.getMessage());
        }



        //inserting "Button 'RELEASE' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STU-Release_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STU-Release_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS RELEASED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'RELEASE' button status message FAILURE: " + e.getMessage());
        }


        //inserting "Button 'SAVE' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STU-Save_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STU-Save_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS SAVED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'SAVE' button status message FAILURE: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\STU-Process_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\STU-Process_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button status message FAILURE: " + e.getMessage());
        }


        //inserting "Button 'PROCESS' status message failed" screenshot link into "log4j-application.html" file
        try {

            //first check if the file was created and saved in the directory
            if (checkFileExists(".\\src\\main\\resources\\error_images\\XSTU-Process_Btn_Status_Message_Failure.png")) {
                String tdLineBeforeChange = "Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message:";
                String baselineScreen = "<a href=" + Paths.get(".\\src\\main\\resources\\error_images\\XSTU-Process_Btn_Status_Message_Failure.png").toRealPath().toUri().toURL() + ">Failure to wait for the 'PAT DOCUMENT WAS PROCESSED' status message</a>";
                String endText = ":";

                //inserting "DATA COLLATION" empty page screenshot links into "log4j-application.html" file
                insertingScreenshotLinksIntoLoggerHTMLfile(itemType_3.toUpperCase(), tdLineBeforeChange, baselineScreen, endText);
            }

            //logger.info("URL Link for Empty DATA COLLATION page WITH ERROR was successfully added to the 'log4j-application.html' file");

        } catch (IOException e) {
            logger.error("Unable to run the 'insertingScreenshotLinksIntoLoggerHTMLfile' method for 'PROCESS' button status message FAILURE: " + e.getMessage());
        }

        //adding test name and document number into a an external text file
        addDocumentNumsToTXT_file(target_System, "PAT_DC_STD-Reversing-XSTU_OHQ_100", doc_Num, doc_Num2, doc_NumReversed);

    }
}
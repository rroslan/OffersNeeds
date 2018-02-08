package biz.eastservices.suara.Common;

/**
 * Created by reale on 2/8/2018.
 */

public class Common {
    //Database define
    public static final String USER_TABLE_CANDIDATE = "Candidates";
    public static final String USER_TABLE_EMPLOYER = "Employers";
    public static final int PICK_IMAGE_REQUEST = 8881;
    public static final int SIGN_IN_REQUEST_CODE = 8888;


    public static String convertTypeToCategory(int type)
    {
        if (type ==0)
            return "Jobs";
        else if (type ==1)
            return "Helps";
        else if (type ==2)
            return "Services";
        else if (type ==3)
            return "Transport";
        else
            return "null";
    }

    public static int convertCategoryToType(String type)
    {
        if (type.equals("Jobs"))
            return 0;
        else if (type.equals("Helps"))
            return 1;
        else if (type.equals("Services"))
            return 2;
        else if (type.equals("Transports"))
            return 3;
        else
            return -1;
    }
}

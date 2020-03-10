package com.group1.EngPlan;


import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;

public class schedule_generator  {
    public static final String LOG_TAG = schedule_generator.class.getSimpleName();
    Cursor data;
    int fYear, wYear;
    int fy = 0, wy = 0, C1Year;

    ArrayList<String> courseID = new ArrayList<>();
    ArrayList<String> courseStatus = new ArrayList<>();

    ArrayList<String> IdealcourseID = new ArrayList<>();
    ArrayList<String> IdealcourseStatus = new ArrayList<>();

    ArrayList<ArrayList<String>> fall_semesters = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> winter_semesters = new ArrayList<ArrayList<String>>();


    DatabaseHandler CDB;

    public schedule_generator(DatabaseHandler db){
        CDB = db;
        data = CDB.getSchedData();
        //test();
    }

    public boolean main(int num_of_courses, String sem, int year){
        add_data();
        fYear = year;
        wYear = year;

        ArrayList<String> MasterList = getMasterList();

        while(!checkMaster(MasterList)){
            ArrayList<String> sub = subList1(MasterList);

            ArrayList<String> Fall = getFall(sub);
            Fall = order(Fall);

            ArrayList<String> Winter = getWinter(sub);
            Winter = order(Winter);

            ArrayList<String> Both = getBoth(sub);
            Both = order(Both);

            if (Fall.size() == 0 && Winter.size() == 0 && Both.size() != 0) {
                Backup(Fall, Winter, Both, num_of_courses);
            }else{
                make_schedule(Fall, Winter,Both, num_of_courses);
            }
        }
        enterInDB(sem);
        return true;
    }

    /*public void test(){
        data.moveToFirst();

            for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
                for(int i = 0; i<9; i++){
                    if(data.getString(i) != null){
                        Log.d(LOG_TAG, data.getString(i));
                    }
                    else{
                        Log.d(LOG_TAG, "null");
                    }

                }
            }
    }*/

    public void add_data(){
        boolean check = true;
        // saving all the data as they are ordered in the Cursor
        data.moveToFirst();
        while (check){
            courseID.add(data.getString(0));
            courseStatus.add(Integer.toString(data.getInt(8)));
            check = data.moveToNext();
        }


        // saving all the data in the order of the ideal schedule
        ArrayList<String> F1 = new ArrayList<>();
        ArrayList<String> W1 = new ArrayList<>();
        ArrayList<String> F2 = new ArrayList<>();
        ArrayList<String> W2 = new ArrayList<>();
        ArrayList<String> F3 = new ArrayList<>();
        ArrayList<String> W3 = new ArrayList<>();
        ArrayList<String> F4 = new ArrayList<>();
        ArrayList<String> W4 = new ArrayList<>();
        ArrayList<String> F5 = new ArrayList<>();
        ArrayList<String> W5 = new ArrayList<>();

        ArrayList<String> sF1 = new ArrayList<>();
        ArrayList<String> sW1 = new ArrayList<>();
        ArrayList<String> sF2 = new ArrayList<>();
        ArrayList<String> sW2 = new ArrayList<>();
        ArrayList<String> sF3 = new ArrayList<>();
        ArrayList<String> sW3 = new ArrayList<>();
        ArrayList<String> sF4 = new ArrayList<>();
        ArrayList<String> sW4 = new ArrayList<>();
        ArrayList<String> sF5 = new ArrayList<>();
        ArrayList<String> sW5 = new ArrayList<>();

        data.moveToFirst();
        check =true;
        while(check){
            switch (data.getString(9)){
                case "F1":
                    F1.add(data.getString(0));
                    sF1.add(Integer.toString(data.getInt(8)));
                    break;
                case "W1":
                    W1.add(data.getString(0));
                    sW1.add(Integer.toString(data.getInt(8)));
                    break;
                case "F2":
                    F2.add(data.getString(0));
                    sF2.add(Integer.toString(data.getInt(8)));
                    break;
                case "W2":
                    W2.add(data.getString(0));
                    sW2.add(Integer.toString(data.getInt(8)));
                    break;
                case "F3":
                    F3.add(data.getString(0));
                    sF3.add(Integer.toString(data.getInt(8)));
                    break;
                case "W3":
                    W3.add(data.getString(0));
                    sW3.add(Integer.toString(data.getInt(8)));
                    break;
                case "F4":
                    F4.add(data.getString(0));
                    sF4.add(Integer.toString(data.getInt(8)));
                    break;
                case "W4":
                    W4.add(data.getString(0));
                    sW4.add(Integer.toString(data.getInt(8)));
                    break;
                case "F5":
                    F5.add(data.getString(0));
                    sF5.add(Integer.toString(data.getInt(8)));
                    break;
                case "W5":
                    W5.add(data.getString(0));
                    sW5.add(Integer.toString(data.getInt(8)));
                    break;
            }

            check = data.moveToNext();
        }

        IdealcourseID.addAll(F1);
        IdealcourseID.addAll(W1);
        IdealcourseID.addAll(F2);
        IdealcourseID.addAll(W2);
        IdealcourseID.addAll(F3);
        IdealcourseID.addAll(W3);
        IdealcourseID.addAll(F4);
        IdealcourseID.addAll(W4);
        IdealcourseID.addAll(F5);
        IdealcourseID.addAll(W5);

        IdealcourseStatus.addAll(sF1);
        IdealcourseStatus.addAll(sW1);
        IdealcourseStatus.addAll(sF2);
        IdealcourseStatus.addAll(sW2);
        IdealcourseStatus.addAll(sF3);
        IdealcourseStatus.addAll(sW3);
        IdealcourseStatus.addAll(sF4);
        IdealcourseStatus.addAll(sW4);
        IdealcourseStatus.addAll(sF5);
        IdealcourseStatus.addAll(sW5);
    }

    public ArrayList<String> getMasterList(){
        ArrayList<String> MasterList = new ArrayList<>();

        for(int i = 0; i<IdealcourseStatus.size(); i++){
            if(IdealcourseStatus.get(i).equals("0")){
                MasterList.add(IdealcourseID.get(i));
            }
        }

        return MasterList;
    }

    public boolean checkMaster(ArrayList<String> Master){


        for(int i = 0; i<Master.size(); i++){
            if(IdealcourseStatus.get(IdealcourseID.indexOf(Master.get(i))).equals("0"))
            return false;
        }
        return true;
    }

    public boolean check_pre_req(String course){
        int pos = courseID.indexOf(course);
        int count = 0;
        String r1, r2;
        if(data.moveToPosition(pos)) {
            if(data.getString(2) == null){
                r1 = "NULL";
            }
            else{
                r1 = data.getString(2);
            }

            if(data.getString(3) == null){
                r2 = "NULL";
            }
            else{
                r2 = data.getString(3);
            }


            if (r1.equals("NULL")) {
                count++;
            } else {
                pos = courseID.indexOf(r1);
                if (courseStatus.get(pos).equals("1")) {
                    count++;
                }
            }


            if (r2.equals("NULL")) {
                count++;
            }
            else {
                pos = courseID.indexOf(r2);
                if (courseStatus.get(pos).equals("1")) {
                    count++;
                }
            }

        }

        return count == 2;
    }

    public ArrayList<String> subList1 (ArrayList<String> masterID ){
        ArrayList<String> s = new ArrayList<>();

        for(int i= 0; i< masterID.size(); i++){
            if(check_pre_req(masterID.get(i)) && courseStatus.get(courseID.indexOf(masterID.get(i))).equals("0")){
                s.add(masterID.get(i));
            }
        }



        return s;
    }

    public ArrayList<String> getFall(ArrayList<String> sub){
        ArrayList<String> fall = new ArrayList<>();

        for(int i = 0; i<sub.size(); i++){
            data.moveToPosition(courseID.indexOf(sub.get(i)));
            if(data.getString(1).equals("F")){
                fall.add(sub.get(i));
            }
        }

        return fall;

    }

    public ArrayList<String> getWinter(ArrayList<String> sub){
        ArrayList<String> Winter = new ArrayList<>();

        for(int i = 0; i<sub.size(); i++){
            data.moveToPosition(courseID.indexOf(sub.get(i)));
            if(data.getString(1).equals("W")){
                Winter.add(sub.get(i));
            }
        }

        return Winter;

    }

    public ArrayList<String> getBoth(ArrayList<String> sub){
        ArrayList<String> Both = new ArrayList<>();

        for(int i = 0; i<sub.size(); i++){
            data.moveToPosition(courseID.indexOf(sub.get(i)));
            if(data.getString(1).equals("B")){
                Both.add(sub.get(i));
            }
        }

        return Both;

    }


    public ArrayList<String> order(ArrayList<String> x){
        int a = -1;
        int count1=0;
        int count2 = 0;
        String m;
        while(a<0){
            for(int i =0; i<=x.size(); i++){
                if(i == x.size()){
                    a = 1;
                    break;
                }
                else{
                    data.moveToPosition(courseID.indexOf(x.get(i)));
                    for(int k = 4; k<8; k++){
                        if(data.getString(k) != null){
                            count1++;
                        }
                    }

                    data.moveToPosition(courseID.indexOf(x.get(i)));
                    for(int k = 4; k<8; k++){
                        if(data.getString(k) != null){
                            count2++;
                        }
                    }

                    if(count1<count2){
                        m = x.get(i+1);
                        x.set(i+1, x.get(i));
                        x.set(i, m);
                        break;
                    }
                }
            }
        }

        return x;
    }

    public Integer getCourseYear(String course){
        switch (course.charAt(4)){
            case '1':
                return 1;
            case '3':
                return 3;
            case '4':
                return 4;
            case '2':
            default:
                return 2;
        }
    }

    public void add_COOP(int Cnum){
        fall_semesters.add(new ArrayList<String>());                                //if it is, then it is scheduled for the year (fy) after the current year (fy)
        fall_semesters.get(fy+1).add("COOP2080");
        IdealcourseStatus.set(IdealcourseID.indexOf("COOP2080"), "1");
        courseStatus.set(courseID.indexOf("COOP2080"), "1");
        C1Year = fy+1;

        if(winter_semesters.size()<= C1Year){
            while(winter_semesters.size()<= C1Year){
                winter_semesters.add(new ArrayList<String>());
            }
        }


        if(winter_semesters.get(C1Year).size() == 0){
            winter_semesters.get(C1Year).add("COOP2180");
            IdealcourseStatus.set(IdealcourseID.indexOf("COOP2180"), "1");
            courseStatus.set(courseID.indexOf("COOP2180"), "1");
        }
        else{
            int i = winter_semesters.get(C1Year).size();
            int j = C1Year+1;
            while(winter_semesters.get(j).size()+i > Cnum){
                j++;
                if(j >= winter_semesters.size()){
                    winter_semesters.add(new ArrayList<String>());
                    break;
                }
            }

            while(winter_semesters.get(C1Year).size()!=0){
                winter_semesters.get(j).add(winter_semesters.get(C1Year).get(0));
                winter_semesters.get(C1Year).remove(0);
            }

            winter_semesters.get(C1Year).add("COOP2180");
            IdealcourseStatus.set(IdealcourseID.indexOf("COOP2180"), "1");
            courseStatus.set(courseID.indexOf("COOP2180"), "1");
        }


    }

    public void make_schedule(ArrayList<String> fall, ArrayList<String> winter, ArrayList<String> both,  int Cnum){
        //ArrayList<String> fall_buffer= new ArrayList<>();
        //ArrayList<String> winter_buffer= new ArrayList<>();
        //int c = 0;
        //int fy = 0, wy = 0;


        fall_semesters.add(new ArrayList<String>());
        winter_semesters.add(new ArrayList<String>());
        while(fall.size()!=0 || winter.size()!=0){
            //Fall semester scheduling
            while(fall_semesters.get(fy).size() < Cnum){
                if(fall_semesters.get(fy).size() != 0 && fall_semesters.get(fy).get(0).equals("COOP2080")){ //checks if the current semester is the coop semester. If it is it goes to the next fall semester
                    fall_semesters.add(new ArrayList<String>());
                    fy++;
                    break;
                }
                else if(fall.size() == 0){                                                                  //checks to see if all the fall courses to be scheduled have been scheduled
                    if (both.size() != 0 && getCourseYear(both.get(0)) <= fy){                      //checks to see if a semester that is available for both semester can be scheduled
                        fall_semesters.get(fy).add(both.get(0));
                        IdealcourseStatus.set(IdealcourseID.indexOf(both.get(0)), "1");
                        courseStatus.set(courseID.indexOf(both.get(0)), "1");
                        both.remove(0);
                    }else{                                                                          //If not, then it breaks out of the loop
                        break;
                    }
                }
                else if(getCourseYear(fall.get(0)) > fYear+fy+1){                                   //this condition makes sure that only courses that are to be taken up till users current year are scheduled. e.g: a 3rd year course is no scheduled in 2nd year
                    fall.remove(0);
                }
                else{
                    if(fall.get(0).equals("COOP2080")){                                             //checks to see if the course to be scheduled is coop 2080
                        add_COOP(Cnum);
                        fall.remove(0);

                    }
                    else {
                        fall_semesters.get(fy).add(fall.get(0));                                    //schedules a normal course
                        IdealcourseStatus.set(IdealcourseID.indexOf(fall.get(0)), "1");
                        courseStatus.set(courseID.indexOf(fall.get(0)), "1");
                        fall.remove(0);
                    }

                }
            }
            if(fall_semesters.get(fy).size() >= Cnum){                                              //makes so that the next years fall semester is scheduled next
                fy++;
                fall_semesters.add(new ArrayList<String>());
            }


            //winter semester scheduling
            Log.d(LOG_TAG, "wy:"+wy);
            Log.d(LOG_TAG, "winter_semesters size:"+winter_semesters.size());
            while(winter_semesters.get(wy).size() < Cnum){
                if(winter.size() == 0){
                    if (both.size() != 0 && getCourseYear(both.get(0)) <= wy){
                        winter_semesters.get(wy).add(both.get(0));
                        IdealcourseStatus.set(IdealcourseID.indexOf(both.get(0)), "1");
                        courseStatus.set(courseID.indexOf(both.get(0)), "1");
                        both.remove(0);
                    }else{
                        break;
                    }
                }
                else if(winter_semesters.get(wy).size() != 0 && winter_semesters.get(wy).get(0).equals("COOP2180")){

                        winter_semesters.add(new ArrayList<String>());
                        wy++;
                        break;

                }
                else if(getCourseYear(winter.get(0)) > wYear+wy+1){
                    winter.remove(0);
                }
                else{
                    if(winter.get(0).equals("COOP2180")){
                        winter.remove(0);
                    }
                    else{
                        winter_semesters.get(wy).add(winter.get(0));
                        IdealcourseStatus.set(IdealcourseID.indexOf(winter.get(0)), "1");
                        courseStatus.set(courseID.indexOf(winter.get(0)), "1");
                        winter.remove(0);
                    }
                }
            }

            if(winter_semesters.get(wy).size() >= Cnum){
                wy++;
                winter_semesters.add(new ArrayList<String>());
            }
        }
    }

    public void Backup(ArrayList<String> fall, ArrayList<String> winter, ArrayList<String> both, int Cnum){
        semester_trim();
        while(both.size()!=0){

            if(fall_semesters.get(fall_semesters.size()-1).size()<Cnum){
                fall_semesters.get(fall_semesters.size()-1).add(both.get(0));
                IdealcourseStatus.set(IdealcourseID.indexOf(both.get(0)), "1");
                courseStatus.set(courseID.indexOf(both.get(0)), "1");
                both.remove(0);
            }
            else if(winter_semesters.get(winter_semesters.size()-1).size()<Cnum){
                winter_semesters.get(winter_semesters.size()-1).add(both.get(0));
                IdealcourseStatus.set(IdealcourseID.indexOf(both.get(0)), "1");
                courseStatus.set(courseID.indexOf(both.get(0)), "1");
                both.remove(0);
            }
            else{
                fall_semesters.add(new ArrayList<String>());
            }


        }
    }

    public void semester_trim(){
        while(fall_semesters.get(fall_semesters.size()-1).size() == 0){
            fall_semesters.remove(fall_semesters.size()-1);
        }

        while(winter_semesters.get(winter_semesters.size()-1).size() == 0){
            winter_semesters.remove(winter_semesters.size()-1);
        }
    }

    public void enterInDB(String sem){
        int Y = 0;
        if(sem.equals("W")){
            fYear ++;
        }

        semester_trim();

        Y = fYear;
        sem = "F";
        for(int i = 0; i<fall_semesters.size(); i++){
                for(int j = 0; j<fall_semesters.get(i).size(); j++){
                    CDB.setSavedSched(fall_semesters.get(i).get(j), sem+Y);
                }
                Y ++;


        }

        Y = wYear;
        sem = "W";
        for(int i = 0; i<winter_semesters.size(); i++){
                for(int j = 0; j<winter_semesters.get(i).size(); j++){
                    CDB.setSavedSched(winter_semesters.get(i).get(j), sem+Y);
                }
                Y ++;

        }

    }
}





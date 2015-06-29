package com.example.dzm.internet;

/**
 * Created by dzm on 6/5/2015.
 */
public class CourseInfo {
    public final static String COURSES = "courses";
    public final static String UNIVERSITIES = "universities";
    public final static String INSTRUCTORS = "instructors";
    public final static String SESSIONS = "sessions";

    public static class Courses implements BaseFields{
        public final static String baseURL = "https://api.coursera.org/api/catalog.v1/courses";
        public static class CoursesEleFields implements BaseEleFields{
            public final static String SHORTNAME = "shortName";
            public final static String NAME = "name";
            public final static String LANGUAGE = "language";
            public final static String SMALLICON = "smallIcon";
            public final static String WORKLOAD = "estimatedClassWorkload";
            public final static String LARGEICON = "largeIcon";
            public final static String BRIEF = "aboutTheCourse";
        }
    }

    public static class Universities implements BaseFields{
        public final static String baseURL = "https://api.coursera.org/api/catalog.v1/universities";

        public static class UniversityEleFields implements BaseEleFields{
            public final static String SHORTNAME = "shortName";
            public final static String NAME = "name";
        }
    }

    public static class Instructors implements BaseFields{
        public final static String baseURL = "https://api.coursera.org/api/catalog.v1/instructors";
    }

    public static class Sessions implements BaseFields{
        public final static String baseURL = "https://api.coursera.org/api/catalog.v1/sessions";
    }

    public static interface BaseEleFields{
        public final static String ID = "id";
        public final static String LINKS = "links";
    }
}

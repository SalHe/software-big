// 该文件专门用于创建整个应用的路由器
import VueRouter from "vue-router";
// 引入组件
import Login from '../pages/Login.vue';
import Register from '../pages/Register.vue';
import HomeStudent from '../pages/HomeStudent.vue';
import Welcome from '../pages/Welcome.vue';
import Exam from '../pages/Exam.vue';
import HomeTeacher from '../pages/HomeTeacher.vue';
import Home from '../pages/Home.vue';
import QuestionBank from '../pages/QuestionBank.vue';
import Course from '../pages/Course.vue';
import Problem from '../pages/Problem.vue';
import ExamManage from '../pages/ExamManage.vue';
import ExamPublish from '../pages/ExamPublish.vue';
import Review from '../pages/Review.vue'
import Vue from "vue";



Vue.use(VueRouter);
// 创建并暴露一个路由器
const router = new VueRouter({
    mode: "history",
    routes: [
        {
            name: 'LoginPage',
            path: '/login',
            component: Login
        },
        {
            name: "RegisterPage",
            path: '/register',
            component: Register,
        },
        {
            name: "HomePage",
            path: '/home',
            component: Home,
            children: [
                {
                    name: "StudentHomePage",
                    path: "student",
                    component: HomeStudent
                },
                {
                    name: "TeacherHomePage",
                    path: "teacher",
                    component: HomeTeacher
                },
            ],
        },
        {
            name: "WelcomePgae",
            path: '/',
            component: Welcome,
            beforeEnter(to, from, next) {
                if (sessionStorage.getItem("token")) {
                    if (sessionStorage.getItem("role") == "学生") {
                        next({ name: "StudentHomePage" });
                    }
                    else if (sessionStorage.getItem("role") == "教师") {
                        next({ name: "TeacherHomePage" });
                    }
                }
                next();
            }
        },
        {
            name: "Exam",
            path: '/exam',
            component: Exam
        },
        {
            name: "QuestionBank",
            path: "/questionbank",
            component: QuestionBank,
        },
        {
            name: "Course",
            path: "/course",
            component: Course
        },
        {
            name: "Problem",
            path: "/problem",
            component: Problem
        },
        {
            name: "ExamManage",
            path: "/exammanage",
            component: ExamManage,
        },
        {
            name: "ExamPublish",
            path: "/exampublish",
            component: ExamPublish
        },
        {
            name: "Review",
            path: "/review",
            component: Review
        },
    ]
});

router.beforeEach((to, from, next) => {
    var role = sessionStorage.getItem('role')
    const routeOfStudent = ['StudentHomePage', 'Exam', 'FaceImg'];
    const routeOfTeacher = ['TeacherHomePage', 'QuestionBank', 'Problem', 'ExamManage', 'ExamPublish', 'Review'];
    if (routeOfStudent.indexOf(to.name) >= 0) {
        if (role == '学生') {
            next();
        }
        else {
            next('/')
        }
    }
    else if (routeOfTeacher.indexOf(to.name) >= 0) {
        if (role == '教师') {
            next();
        } else {
            next('/')
        }
    }
    else {
        next();
    }

});
export default router;
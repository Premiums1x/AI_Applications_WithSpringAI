package com.lancer.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.lancer.ai.entity.po.Course;
import com.lancer.ai.entity.po.CourseReservation;
import com.lancer.ai.entity.po.School;
import com.lancer.ai.entity.query.CourseQuery;
import com.lancer.ai.service.ICourseReservationService;
import com.lancer.ai.service.ICourseService;
import com.lancer.ai.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {


//    Service层实现类：
//    构造函数方法注入bean
    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(@ToolParam(description = "查询课程条件",required = false) CourseQuery query){
        if (query == null){
            return List.of();
//            查询条件为空返回空list
        }

//        Service实现类：
        QueryChainWrapper<Course> courseQueryChainWrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());// <=
//        先拿到存起来，后续的查询条件看情况添加：

//        如果有升降序字段，就补上查询条件：
        if (query.getSorts() != null && !query.getSorts().isEmpty()){
            for (CourseQuery.Sort sort :query.getSorts()){
//                true要加上查询条件
                courseQueryChainWrapper.orderBy(true, sort.getAsc(),sort.getField());
            }
        }

//    最终查询结果：
            return courseService.list();

    }

    //    查校区：
        @Tool(description = "查询所有校区")
        public List<School> querySchool(){
    //        调用服务层的方法直接返回所有，没有查询条件
            return schoolService.list();
    }


//    创建预约订单：
    @Tool(description = "生成预约单,返回预约单号")
    public Integer createCourseReservation(
            @ToolParam(description = "预约课程") String course,
            @ToolParam(description = "预约校区") String school,
            @ToolParam(description = "学生姓名") String studentName,
            @ToolParam(description = "联系方式") String contactInfo,
            @ToolParam(description = "备注",required = false) String remark
    ){
//        封装成对象：
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(course);
        courseReservation.setSchool(school);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setRemark(remark);

        courseReservationService.save(courseReservation);

//        拿到订单号回显
        return courseReservation.getId();


    }




}

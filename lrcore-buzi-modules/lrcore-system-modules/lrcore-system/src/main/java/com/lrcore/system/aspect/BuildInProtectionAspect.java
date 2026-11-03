//package com.lrcore.system.aspect;
//
//import com.lrcore.common.core.constant.SecurityConstants;
//import com.lrcore.common.core.exception.ServiceException;
//import com.lrcore.common.core.utils.ServletUtils;
//import com.lrcore.common.core.web.domain.BaseEntity;
//import com.mybatisflex.core.service.IService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.core.annotation.AnnotatedElementUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.io.Serializable;
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.IntStream;
//import java.util.stream.Stream;
//
//@Aspect
//@Component
//public class BuildInProtectionAspect {
//
//    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void restController() {
//    }
//
//    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || "
//            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
//            + "@annotation(org.springframework.web.bind.annotation.PatchMapping) || "
//            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
//    public void writeOperation() {
//    }
//
//    @Before("restController() && writeOperation()")
//    public void protectBuildInData(JoinPoint joinPoint) {
//        if (isInnerRequest()) {
//            return;
//        }
//
//        Object controller = joinPoint.getTarget();
//        for (Field field : controller.getClass().getDeclaredFields()) {
//            if (!IService.class.isAssignableFrom(field.getType())) {
//                continue;
//            }
//            field.setAccessible(true);
//            IService<?> service = (IService<?>) fieldValue(controller, field);
//            if (Objects.isNull(service)) {
//                continue;
//            }
//
//            checkRequestData(joinPoint, service);
//        }
//    }
//
//    private boolean isInnerRequest() {
//        HttpServletRequest request = ServletUtils.getRequest();
//        return Objects.nonNull(request)
//                && SecurityConstants.INNER.equals(request.getHeader(SecurityConstants.FROM_SOURCE));
//    }
//
//    private void checkRequestData(JoinPoint joinPoint, IService<?> service) {
//        for (Object argument : joinPoint.getArgs()) {
//            if (argument instanceof BaseEntity entity) {
//                if (Objects.equals(1, buildInValue(entity))) {
//                    throw new ServiceException("系统内置数据不允许页面操作");
//                }
//                if (Objects.nonNull(entity.getId())) {
//                    assertNotBuildIn(service, entity.getId());
//                }
//            }
//        }
//
//        for (Object id : pathVariableIds(joinPoint)) {
//            assertNotBuildIn(service, id);
//        }
//    }
//
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    private void assertNotBuildIn(IService<?> service, Object id) {
//        if (!(id instanceof Serializable serializableId)) {
//            return;
//        }
//        Object entity = ((IService) service).getById(serializableId);
//        if (Objects.equals(1, buildInValue(entity))) {
//            throw new ServiceException("系统内置数据不允许页面操作");
//        }
//    }
//
//    private Object buildInValue(Object entity) {
//        if (!(entity instanceof BaseEntity)) {
//            return null;
//        }
//        try {
//            Field field = entity.getClass().getDeclaredField("buildIn");
//            field.setAccessible(true);
//            return field.get(entity);
//        } catch (NoSuchFieldException | IllegalAccessException exception) {
//            return null;
//        }
//    }
//
//    private List<Object> pathVariableIds(JoinPoint joinPoint) {
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Object[] arguments = joinPoint.getArgs();
//        java.lang.annotation.Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
//        return IntStream.range(0, arguments.length)
//                .filter(index -> isIdPathVariable(signature, index, parameterAnnotations[index]))
//                .mapToObj(index -> arguments[index])
//                .filter(Objects::nonNull)
//                .flatMap(id -> id instanceof String value && value.contains(",")
//                        ? Arrays.stream(value.split(",")).filter(item -> !item.isBlank())
//                        : Stream.of(id))
//                .toList();
//    }
//
//    private boolean isIdPathVariable(MethodSignature signature, int parameterIndex,
//                                     java.lang.annotation.Annotation[] annotations) {
//        return Arrays.stream(annotations)
//                .map(annotation -> AnnotatedElementUtils.findMergedAnnotation(
//                        annotation.annotationType(), PathVariable.class))
//                .filter(Objects::nonNull)
//                .map(pathVariable -> pathVariable.value().isBlank()
//                        ? signature.getMethod().getParameters()[parameterIndex].getName()
//                        : pathVariable.value())
//                .anyMatch(name -> "id".equals(name) || "ids".equals(name));
//    }
//
//    private Object fieldValue(Object target, Field field) {
//        try {
//            return field.get(target);
//        } catch (IllegalAccessException exception) {
//            return null;
//        }
//    }
//}

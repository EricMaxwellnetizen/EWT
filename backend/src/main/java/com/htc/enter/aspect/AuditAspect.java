package com.htc.enter.aspect;

import com.htc.enter.model.AuditLog;
import com.htc.enter.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Intercept all JPA repository save operations to automatically log CREATE and UPDATE operations
     * Uses AOP @Around advice to capture entity state before and after the save
     * 
     * @param joinPoint - The intercepted method execution point
     * @param entity - The entity being saved
     * @return The saved entity (result of the intercepted save operation)
     * @throws Throwable if the save operation fails
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.save(..)) && args(entity)")
    public Object auditSave(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        // Get entity ID before save (null for new entities)
        Long entityIdBeforeSave = getEntityId(entity);
        boolean isCreateOperation = (entityIdBeforeSave == null);
        
        // Proceed with the save operation
        Object savedEntity = joinPoint.proceed();
        
        // Get entity ID after save
        Long entityIdAfterSave = getEntityId(savedEntity);
        
        // Log the operation
        String entityTypeName = entity.getClass().getSimpleName();
        AuditLog.OperationType operationType = isCreateOperation ? 
            AuditLog.OperationType.CREATE : 
            AuditLog.OperationType.UPDATE;
        String operationDescription = isCreateOperation ? 
            "Created new " + entityTypeName : 
            "Updated " + entityTypeName;
        
        auditLogService.logOperation(
            entityTypeName,
            entityIdAfterSave,
            operationType,
            isCreateOperation ? null : entity, // old value (null for create)
            savedEntity, // new value
            operationDescription
        );
        
        return savedEntity;
    }

    /**
     * Intercept all JPA repository delete operations to automatically log deletions
     * Captures entity state before deletion
     * 
     * @param joinPoint - The intercepted method execution point
     * @param entity - The entity being deleted
     * @return The result of the delete operation
     * @throws Throwable if the delete operation fails
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.delete(..)) && args(entity)")
    public Object auditDelete(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        Long entityIdBeforeDeletion = getEntityId(entity);
        String entityTypeName = entity.getClass().getSimpleName();
        
        // Proceed with the delete operation
        Object deleteResult = joinPoint.proceed();
        
        // Log the operation
        auditLogService.logOperation(
            entityTypeName,
            entityIdBeforeDeletion,
            AuditLog.OperationType.DELETE,
            entity, // old value (entity before deletion)
            null, // new value (null after deletion)
            "Deleted " + entityTypeName
        );
        
        return deleteResult;
    }

    /**
     * Intercept all JPA repository deleteById operations to automatically log deletions
     * Note: Entity data is not available when deleting by ID
     * 
     * @param joinPoint - The intercepted method execution point
     * @param id - The ID of the entity being deleted
     * @return The result of the delete operation
     * @throws Throwable if the delete operation fails
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.deleteById(..)) && args(id)")
    public Object auditDeleteById(ProceedingJoinPoint joinPoint, Long id) throws Throwable {
        // Get the repository instance to determine entity type
        Object repositoryInstance = joinPoint.getTarget();
        String repositoryClassName = repositoryInstance.getClass().getSimpleName();
        String entityTypeName = repositoryClassName.replace("Repository", "");
        
        // Proceed with the delete operation
        Object deleteResult = joinPoint.proceed();
        
        // Log the operation
        auditLogService.logOperation(
            entityTypeName,
            id,
            AuditLog.OperationType.DELETE,
            null, // old value not available in deleteById
            null, // new value (null after deletion)
            "Deleted " + entityTypeName + " by ID"
        );
        
        return deleteResult;
    }

    /**
     * Helper method to extract entity ID using reflection
     * Searches for an "id" field in the entity class hierarchy
     * 
     * @param entity - The entity object
     * @return Entity ID as Long, or null if not found or not a numeric type
     */
    private Long getEntityId(Object entity) {
        if (entity == null) return null;
        
        try {
            // Try to find 'id' field in class hierarchy
            Field idField = findIdField(entity.getClass());
            if (idField != null) {
                idField.setAccessible(true);
                Object idValue = idField.get(entity);
                if (idValue instanceof Long) {
                    return (Long) idValue;
                } else if (idValue instanceof Integer) {
                    return ((Integer) idValue).longValue();
                }
            }
        } catch (Exception reflectionException) {
            // Silently ignore exceptions
        }
        
        return null;
    }

    /**
     * Find the ID field in the entity class hierarchy
     * Recursively searches parent classes if not found in current class
     * 
     * @param entityClass - The class to search
     * @return Field object representing the "id" field, or null if not found
     */
    private Field findIdField(Class<?> entityClass) {
        if (entityClass == null || entityClass == Object.class) {
            return null;
        }
        
        try {
            return entityClass.getDeclaredField("id");
        } catch (NoSuchFieldException fieldNotFoundException) {
            // Try parent class recursively
            return findIdField(entityClass.getSuperclass());
        }
    }
}

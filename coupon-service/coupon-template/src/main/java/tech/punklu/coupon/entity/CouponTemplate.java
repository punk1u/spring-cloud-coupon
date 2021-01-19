package tech.punklu.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tech.punklu.coupon.constant.CouponCategory;
import tech.punklu.coupon.constant.DistributeTarget;
import tech.punklu.coupon.constant.ProductLine;
import tech.punklu.coupon.converter.CouponCategoryConverter;
import tech.punklu.coupon.converter.DistributeTargetConverter;
import tech.punklu.coupon.converter.ProductLineConverter;
import tech.punklu.coupon.converter.RuleConverter;
import tech.punklu.coupon.serialization.CouponTemplateSerialize;
import tech.punklu.coupon.vo.TemplateRule;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 优惠券模板实体类定义:基础属性 + 规则属性
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // 以实现列自动赋值
@Table(name = "coupon_template")
@JsonSerialize(using = CouponTemplateSerialize.class) // 通过自定义的序列化器来指定返回给前端的实体类字段自定义内容
public class CouponTemplate implements Serializable {

    /**
     *自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Integer id;

    /**
     * 模板状态是否可用
     */
    @Column(name = "available",nullable = false)
    private Boolean available;

    /**
     * 是否过期
     */
    @Column(name = "expired",nullable = false)
    private Boolean expired;

    /**
     * 优惠券名称
     */
    @Column(name = "name",nullable = false)
    private String name;

    /**
     * 优惠券logo
     */
    @Column(name = "logo",nullable = false)
    private String logo;

    /**
     * 优惠券描述
     */
    @Column(name = "intro",nullable = false)
    private String desc;

    /**
     * 优惠券分类
     */
    @Convert(converter = CouponCategoryConverter.class) // 实现数据库字段值和CouponCategory的相互转换
    @Column(name = "category",nullable = false)
    private CouponCategory category;

    // 产品线
    @Convert(converter = ProductLineConverter.class) // 实现数据库字段值和ProductLine的相互转换
    @Column(name = "product_line",nullable = false)
    private ProductLine productLine;

    // 总数
    @Column(name = "coupon_count",nullable = false)
    private Integer count;

    // 创建时间
    @CreatedDate // 新增时自动填充日期
    @UpdateTimestamp
    @CreatedBy
    @Column(name = "create_time",nullable = false)
    private Date createTime;

    // 创建用户
    @Column(name = "user_id",nullable = false)
    private Long userId;

    // 优惠券模板编码
    @Column(name = "template_key",nullable = false)
    private String key;

    // 目标用户
    @Convert(converter = DistributeTargetConverter.class) // 实现数据库字段值和DistributeTarget的相互转换
    @Column(name = "target",nullable = false)
    private DistributeTarget target;

    // 优惠券规则
    @Convert(converter = RuleConverter.class) // 实现数据库字段值TemplateRule的相互转换
    @Column(name = "rule",nullable = false)
    private TemplateRule rule;

    /**
     * 自定义构造函数
     * @param name
     * @param logo
     * @param desc
     * @param category
     * @param productLine
     * @param count
     * @param userId
     * @param target
     * @param rule
     */
    public CouponTemplate(String name,String logo,String desc,String category,
                          Integer productLine,Integer count, Long userId,
                          Integer target,TemplateRule rule){
        this.available = false;
        this.expired = false;
        this.name = name;
        this.logo = logo;
        this.desc = desc;
        this.category = CouponCategory.of(category);
        this.productLine = ProductLine.of(productLine);
        this.count = count;
        this.userId = userId;
        // 优惠券模板唯一编码 = 4（产品线 + 类型） + 8（日期） + id（扩充为4位）
        this.key = productLine.toString() + category + new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.target = DistributeTarget.of(target);
        this.rule = rule;
    }
}

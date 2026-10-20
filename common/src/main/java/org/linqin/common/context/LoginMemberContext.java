package org.linqin.common.context;

import org.linqin.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录上下文
 * 它是一个**基于 ThreadLocal 的登录用户上下文工具类**，用来在当前请求线程里随时拿到 "当前登录用户是谁"，不用层层传参
 */
public class LoginMemberContext {
    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    // 新建一个的线程，存储当前登录会员信息Member副本
    private static final ThreadLocal<MemberLoginResp> member = new ThreadLocal<>();

    // - 从当前线程的 ThreadLocal 里取出登录用户信息。
    //- 如果没 set 过，返回 null。
    public static MemberLoginResp getMember() {
        return member.get();
    }

    // MemberInterceptor拦截器在请求进来时调用，拦截器创建MemberLoginResp返回类，将返回类放入Context，
    // 把登录用户信息存进当前线程的 ThreadLocal
    public static void setMember(MemberLoginResp member) {
        LoginMemberContext.member.set(member);
    }

    // 从返回体中取出用户id
    public static Long getId() {
        try {
            // member.get()获取MemberLoginResp，再获取它的id
            return member.get().getId();
        } catch (Exception e) {
            // 比如member.get () 是 null（用户没登录就访问了需要登录的接口），会抛空指针，这里捕获后打错误日志再抛出去。
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }
}


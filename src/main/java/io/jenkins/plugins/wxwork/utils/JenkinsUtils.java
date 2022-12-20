package io.jenkins.plugins.wxwork.utils;

import groovy.util.logging.Slf4j;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import io.jenkins.plugins.wxwork.WXWorkUserExtensionProperty;
import io.jenkins.plugins.wxwork.bo.RunUser;

import java.util.stream.Collectors;

/**
 * <p>JenkinsUtils</p>
 *
 * @author nekoimi 2022/11/14
 */
@Slf4j
public class JenkinsUtils {

    private JenkinsUtils() {
    }

    /**
     * <p>获取执行用户</p>
     *
     * @param run {@link Run}
     * @param listener {@link TaskListener}
     */
    public static RunUser getRunUser(Run<?, ?> run, TaskListener listener) {
        Cause.UserIdCause userIdCause = run.getCause(Cause.UserIdCause.class);
        // 执行人信息
        User user = null;
        String executorName;
        String executorMobile = null;
        if (userIdCause != null && userIdCause.getUserId() != null) {
            user = User.getById(userIdCause.getUserId(), false);
        }
        if (user == null) {
            listener.error("未获取到构建人信息，将尝试从构建信息中模糊匹配。");
            executorName = run.getCauses().stream().map(Cause::getShortDescription).collect(Collectors.joining());
        } else {
            executorName = user.getDisplayName();
            WXWorkUserExtensionProperty executorProperty = user.getProperty(WXWorkUserExtensionProperty.class);
            if (executorProperty == null) {
                listener.error("用户【%s】暂未设置手机号码，请前往 %s 添加。", executorName, user.getAbsoluteUrl() + "/configure");
            } else {
                executorMobile = executorProperty.getMobile();
                if (StrUtils.isBlank(executorMobile)){
                    listener.error("用户【%s】暂未设置手机号码，请前往 %s 添加。", executorName, user.getAbsoluteUrl() + "/configure");
                }
            }
        }
        return RunUser.builder().name(executorName).mobile(executorMobile).build();
    }
}

package org.cookpro.tools.alibaba;

import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

/**
 * Alibaba Tool接口，构建时需要配合 {@linkplain org.cookpro.utils.ToolUtils}  <p>
 * {@code org.cookpro.utils.ToolUtils.buildToolCallback(new BaseAlibabaToolInter{});},
 *
 *
 *
 * @param <T> 输入参数类型
 * @param <R> 输出结果类型
 */
public  interface BaseAlibabaToolInter<T,R> extends BiFunction<T, ToolContext,R> {
  String AGENT_CONFIG = "_AGENT_CONFIG_";
  String getToolName();
  String getToolDescription();

  Class<T> getInputType();
}

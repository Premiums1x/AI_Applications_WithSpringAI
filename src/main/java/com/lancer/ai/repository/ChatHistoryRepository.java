package com.lancer.ai.repository;

import java.util.List;

public interface ChatHistoryRepository {

    /**
     *
     * @param type:业务类型
     * @param chatID：会话ID
     */
    public void save(String type,String chatID);


    /**
     *
     * @param type：业务类型
     * @return 所以id列表
     */
    public List<String> getChatIDs(String type);

}

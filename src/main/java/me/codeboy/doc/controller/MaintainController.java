package me.codeboy.doc.controller;

import me.codeboy.doc.base.CommonResult;
import me.codeboy.doc.data.DocUpdater;
import me.codeboy.doc.db.helper.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据维护
 * Created by yuedong.li on 2019-12-21
 */
@RestController
public class MaintainController {
    private static final Logger logger = LoggerFactory.getLogger(MaintainController.class);
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private DocUpdater docUpdater;

    @RequestMapping(value = "/admin/createIndex")
    public CommonResult createEsIndex() {
        logger.info("create es index");
        if (dbHelper.createEsIndex()) {
            return CommonResult.success("创建成功");
        } else {
            return CommonResult.failed("创建失败，可能索引是已经存在");
        }
    }

    @RequestMapping(value = "/admin/update")
    public CommonResult updateDoc() {
        logger.info("start update doc");
        executorService.submit(() -> docUpdater.updateDoc());
        return CommonResult.success("操作成功");
    }

    @RequestMapping(value = "/admin/reset")
    public CommonResult resetDoc() {
        logger.info("start reset doc");
        if (dbHelper.resetEsData() && dbHelper.deleteAllDocs()) {
            return CommonResult.success("数据已清空");
        } else {
            return CommonResult.failed("数据清空失败");
        }
    }

}

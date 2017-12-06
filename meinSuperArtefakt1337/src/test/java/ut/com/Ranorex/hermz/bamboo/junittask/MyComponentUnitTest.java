package ut.com.Ranorex.hermz.bamboo.junittask;

import org.junit.Test;
import com.Ranorex.hermz.bamboo.junittask.api.MyPluginComponent;
import com.Ranorex.hermz.bamboo.junittask.impl.MyPluginComponentImpl;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        //assertEquals("names do not match!", "myComponent",component.getName());
    }
}
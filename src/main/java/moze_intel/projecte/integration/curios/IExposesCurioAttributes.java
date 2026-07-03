package moze_intel.projecte.integration.curios;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface IExposesCurioAttributes {

	void addAttributes(Multimap<Holder<Attribute>, AttributeModifier> attributes);
}
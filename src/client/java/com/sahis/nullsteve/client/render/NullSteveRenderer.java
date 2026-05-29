package com.sahis.nullsteve.client.render;

import com.sahis.nullsteve.entity.NullSteveEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class NullSteveRenderer extends MobEntityRenderer<NullSteveEntity, PlayerEntityModel<NullSteveEntity>> {

private static final Identifier TEXTURE =
    Identifier.of("null_steve", "textures/entity/null_steve.png");

public NullSteveRenderer(EntityRendererFactory.Context ctx) {
    super(ctx, makeModel(ctx), 0.5f);
}

private static PlayerEntityModel<NullSteveEntity> makeModel(EntityRendererFactory.Context ctx) {
    ModelPart root = ctx.getPart(EntityModelLayers.PLAYER);
    return new PlayerEntityModel<>(root, false);
}

@Override
public Identifier getTexture(NullSteveEntity entity) {
    return TEXTURE;
}
}

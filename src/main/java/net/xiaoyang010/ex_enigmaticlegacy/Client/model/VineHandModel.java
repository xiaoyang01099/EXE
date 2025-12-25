package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class VineHandModel<T extends LivingEntity> extends HumanoidModel<T> {
	private final ModelPart liebe;
	private final ModelPart q;
	private final ModelPart e;
	private final ModelPart group29;
	private final ModelPart group30;
	private final ModelPart group31;
	private final ModelPart group32;
	private final ModelPart group33;
	private final ModelPart group34;
	private final ModelPart group35;
	private final ModelPart r;
	private final ModelPart group36;
	private final ModelPart group37;
	private final ModelPart group38;
	private final ModelPart group39;
	private final ModelPart group40;
	private final ModelPart group41;
	private final ModelPart group42;
	private final ModelPart t;
	private final ModelPart group43;
	private final ModelPart group44;
	private final ModelPart group45;
	private final ModelPart group46;
	private final ModelPart group47;
	private final ModelPart group48;
	private final ModelPart group49;
	private final ModelPart y;
	private final ModelPart group50;
	private final ModelPart group51;
	private final ModelPart group52;
	private final ModelPart group53;
	private final ModelPart group54;
	private final ModelPart group55;
	private final ModelPart group56;
	private final ModelPart w;
	private final ModelPart u;
	private final ModelPart i;
	private final ModelPart o;
	private final ModelPart a;
	private final ModelPart s;
	private final ModelPart d;
	private final ModelPart f;
	private final ModelPart liebe2;
	private final ModelPart qw;
	private final ModelPart qe;
	private final ModelPart group2;
	private final ModelPart group3;
	private final ModelPart group4;
	private final ModelPart group5;
	private final ModelPart group6;
	private final ModelPart group7;
	private final ModelPart group8;
	private final ModelPart we;
	private final ModelPart group9;
	private final ModelPart group10;
	private final ModelPart group11;
	private final ModelPart group12;
	private final ModelPart group13;
	private final ModelPart group14;
	private final ModelPart group15;
	private final ModelPart wq;
	private final ModelPart group16;
	private final ModelPart group17;
	private final ModelPart group18;
	private final ModelPart group19;
	private final ModelPart group20;
	private final ModelPart group21;
	private final ModelPart group22;
	private final ModelPart eq;
	private final ModelPart group23;
	private final ModelPart group24;
	private final ModelPart group25;
	private final ModelPart group26;
	private final ModelPart group27;
	private final ModelPart group28;
	private final ModelPart group;
	private final ModelPart as;
	private final ModelPart sa;
	private final ModelPart sd;
	private final ModelPart ds;
	private final ModelPart df;
	private final ModelPart fd;
	private final ModelPart fg;
	private final ModelPart gf;

	public VineHandModel(ModelPart root) {
        super(root);
        this.liebe = root.getChild("liebe");
		this.q = this.liebe.getChild("q");
		this.e = this.q.getChild("e");
		this.group29 = this.e.getChild("group29");
		this.group30 = this.e.getChild("group30");
		this.group31 = this.group30.getChild("group31");
		this.group32 = this.group30.getChild("group32");
		this.group33 = this.group30.getChild("group33");
		this.group34 = this.group30.getChild("group34");
		this.group35 = this.e.getChild("group35");
		this.r = this.q.getChild("r");
		this.group36 = this.r.getChild("group36");
		this.group37 = this.r.getChild("group37");
		this.group38 = this.group37.getChild("group38");
		this.group39 = this.group37.getChild("group39");
		this.group40 = this.group37.getChild("group40");
		this.group41 = this.group37.getChild("group41");
		this.group42 = this.r.getChild("group42");
		this.t = this.q.getChild("t");
		this.group43 = this.t.getChild("group43");
		this.group44 = this.t.getChild("group44");
		this.group45 = this.group44.getChild("group45");
		this.group46 = this.group44.getChild("group46");
		this.group47 = this.group44.getChild("group47");
		this.group48 = this.group44.getChild("group48");
		this.group49 = this.t.getChild("group49");
		this.y = this.q.getChild("y");
		this.group50 = this.y.getChild("group50");
		this.group51 = this.y.getChild("group51");
		this.group52 = this.group51.getChild("group52");
		this.group53 = this.group51.getChild("group53");
		this.group54 = this.group51.getChild("group54");
		this.group55 = this.group51.getChild("group55");
		this.group56 = this.y.getChild("group56");
		this.w = this.liebe.getChild("w");
		this.u = this.w.getChild("u");
		this.i = this.w.getChild("i");
		this.o = this.w.getChild("o");
		this.a = this.w.getChild("a");
		this.s = this.w.getChild("s");
		this.d = this.w.getChild("d");
		this.f = this.w.getChild("f");
		this.liebe2 = root.getChild("liebe2");
		this.qw = this.liebe2.getChild("qw");
		this.qe = this.qw.getChild("qe");
		this.group2 = this.qe.getChild("group2");
		this.group3 = this.qe.getChild("group3");
		this.group4 = this.group3.getChild("group4");
		this.group5 = this.group3.getChild("group5");
		this.group6 = this.group3.getChild("group6");
		this.group7 = this.group3.getChild("group7");
		this.group8 = this.qe.getChild("group8");
		this.we = this.qw.getChild("we");
		this.group9 = this.we.getChild("group9");
		this.group10 = this.we.getChild("group10");
		this.group11 = this.group10.getChild("group11");
		this.group12 = this.group10.getChild("group12");
		this.group13 = this.group10.getChild("group13");
		this.group14 = this.group10.getChild("group14");
		this.group15 = this.we.getChild("group15");
		this.wq = this.qw.getChild("wq");
		this.group16 = this.wq.getChild("group16");
		this.group17 = this.wq.getChild("group17");
		this.group18 = this.group17.getChild("group18");
		this.group19 = this.group17.getChild("group19");
		this.group20 = this.group17.getChild("group20");
		this.group21 = this.group17.getChild("group21");
		this.group22 = this.wq.getChild("group22");
		this.eq = this.qw.getChild("eq");
		this.group23 = this.eq.getChild("group23");
		this.group24 = this.eq.getChild("group24");
		this.group25 = this.group24.getChild("group25");
		this.group26 = this.group24.getChild("group26");
		this.group27 = this.group24.getChild("group27");
		this.group28 = this.group24.getChild("group28");
		this.group = this.eq.getChild("group");
		this.as = this.liebe2.getChild("as");
		this.sa = this.as.getChild("sa");
		this.sd = this.as.getChild("sd");
		this.ds = this.as.getChild("ds");
		this.df = this.as.getChild("df");
		this.fd = this.as.getChild("fd");
		this.fg = this.as.getChild("fg");
		this.gf = this.as.getChild("gf");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition liebe = partdefinition.addOrReplaceChild("liebe", CubeListBuilder.create(), PartPose.offset(-21.0F, 2.0F, 0.0F));

		PartDefinition q = liebe.addOrReplaceChild("q", CubeListBuilder.create(), PartPose.offset(5.0F, 14.0F, 0.0F));

		PartDefinition e = q.addOrReplaceChild("e", CubeListBuilder.create(), PartPose.offset(-7.4477F, 0.0F, 0.0F));

		PartDefinition group29 = e.addOrReplaceChild("group29", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(9.5618F, -12.3443F, 5.4116F, 1.23F, 0.779F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.3568F, -12.0778F, 5.4321F, 0.205F, 0.5125F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(10.7881F, -12.0778F, 5.4321F, 0.205F, 0.5125F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.3158F, -11.5653F, 5.4116F, 1.722F, 0.861F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.7668F, -12.5493F, 5.4116F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.9718F, -12.7543F, 5.4116F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.6439F, -10.4993F, 5.4321F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.8488F, -10.2943F, 5.4321F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.4388F, -10.7043F, 5.4321F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group30 = e.addOrReplaceChild("group30", CubeListBuilder.create().texOffs(64, 43).addBox(8.2869F, -12.6943F, 5.4981F, 1.23F, 0.779F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.0818F, -12.4278F, 5.5186F, 0.205F, 0.5125F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5131F, -12.4278F, 5.5186F, 0.205F, 0.5125F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(64, 12).addBox(8.0409F, -11.9153F, 5.4981F, 1.722F, 0.861F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4918F, -12.8993F, 5.4981F, 0.82F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6969F, -13.1043F, 5.4981F, 0.41F, 0.205F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.3689F, -10.8493F, 5.5186F, 1.066F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.5739F, -10.6443F, 5.5186F, 0.6765F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1639F, -11.0543F, 5.5186F, 1.476F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7923F, -13.2213F, 5.3956F, 0.2255F, 2.665F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r1 = group30.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0675F, 0.0F, 0.2255F, 0.901F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0212F, -10.5008F, 5.3956F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group31 = group30.addOrReplaceChild("group31", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r2 = group31.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -10.5903F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r3 = group31.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -10.7543F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r4 = group31.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -11.1028F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r5 = group31.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -11.2668F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r6 = group31.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.3895F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -11.7383F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r7 = group31.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -11.5743F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r8 = group31.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.2665F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -12.0253F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r9 = group31.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.287F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3806F, -12.1278F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition group32 = group30.addOrReplaceChild("group32", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r10 = group32.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0769F, -10.3443F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r11 = group32.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -10.5083F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r12 = group32.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0769F, -10.8568F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r13 = group32.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -11.0208F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r14 = group32.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, -0.1263F, 0.0F, 0.5535F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -11.4308F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r15 = group32.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0769F, -11.2668F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r16 = group32.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, -0.1263F, 0.0F, 0.3075F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9333F, -11.8408F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r17 = group32.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, -0.1263F, 0.0F, 0.287F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7079F, -11.9433F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition group33 = group30.addOrReplaceChild("group33", CubeListBuilder.create().texOffs(0, 0).addBox(8.1232F, -12.8653F, 5.4501F, 0.205F, 0.7995F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8783F, -11.5063F, 5.4501F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9924F, -12.8653F, 5.4501F, 0.205F, 0.7995F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r18 = group33.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1885F, 0.0098F, 0.0F, 0.205F, 1.0865F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1974F, -12.0863F, 5.4706F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r19 = group33.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0166F, 0.0098F, 0.0F, 0.205F, 1.0865F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1232F, -12.0863F, 5.4706F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r20 = group33.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5947F, -13.4803F, 5.4706F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r21 = group33.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0867F, -13.9518F, 5.4501F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r22 = group33.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1482F, -14.1363F, 5.4501F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r23 = group33.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, -0.0648F, 0.0F, 0.287F, 0.3075F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1892F, -14.3618F, 5.4706F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r24 = group33.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, -0.0033F, 0.0F, 0.205F, 0.2255F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2507F, -14.2388F, 5.4501F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r25 = group33.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1724F, -14.1363F, 5.4501F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r26 = group33.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2339F, -13.9518F, 5.4501F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r27 = group33.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7259F, -13.4803F, 5.4706F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group34 = group30.addOrReplaceChild("group34", CubeListBuilder.create().texOffs(0, 0).addBox(8.258F, -12.8586F, 5.5058F, 0.1784F, 0.6956F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.915F, -11.6762F, 5.5058F, 0.4816F, 0.1783F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8842F, -12.8586F, 5.5058F, 0.1784F, 0.6956F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r28 = group34.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1639F, 0.0085F, 0.0F, 0.1784F, 0.9453F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0625F, -12.1808F, 5.5236F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r29 = group34.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0144F, 0.0085F, 0.0F, 0.1784F, 0.9453F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.258F, -12.1808F, 5.5236F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r30 = group34.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6682F, -13.3936F, 5.5236F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r31 = group34.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0962F, -13.8038F, 5.5058F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r32 = group34.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1497F, -13.9643F, 5.5058F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r33 = group34.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, -0.0564F, 0.0F, 0.2497F, 0.2675F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1854F, -14.1605F, 5.5236F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r34 = group34.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, -0.0029F, 0.0F, 0.1784F, 0.1962F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2389F, -14.0535F, 5.5058F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r35 = group34.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1708F, -13.9643F, 5.5058F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r36 = group34.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2243F, -13.8038F, 5.5058F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r37 = group34.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6523F, -13.3936F, 5.5236F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group35 = e.addOrReplaceChild("group35", CubeListBuilder.create().texOffs(40, 64).addBox(8.3243F, -12.6444F, 5.6465F, 1.1562F, 0.7323F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1316F, -12.3939F, 5.6658F, 0.1927F, 0.4817F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.477F, -12.3939F, 5.6658F, 0.1927F, 0.4817F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(28, 64).addBox(8.093F, -11.9122F, 5.6465F, 1.6187F, 0.8093F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.517F, -12.8371F, 5.6465F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7096F, -13.0298F, 5.6465F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4013F, -10.9101F, 5.6658F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.594F, -10.7174F, 5.6658F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.2086F, -11.1028F, 5.6658F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6452F, -10.6347F, 5.5887F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7994F, -10.8274F, 5.5887F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9354F, -11.9122F, 5.5887F, 0.1927F, 0.7515F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.6924F, -11.9122F, 5.5887F, 0.1927F, 0.7515F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r38 = group35.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1771F, 0.0092F, 0.0F, 0.1927F, 1.0213F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.8851F, -11.1799F, 5.608F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r39 = group35.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4419F, -12.4903F, 5.608F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r40 = group35.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9216F, -13.1069F, 5.5887F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r41 = group35.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9794F, -12.9335F, 5.5887F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r42 = group35.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, -0.0031F, 0.0F, 0.1927F, 0.212F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9952F, -13.2033F, 5.5887F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r43 = group35.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, -0.061F, 0.0F, 0.2698F, 0.289F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9374F, -13.3189F, 5.608F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r44 = group35.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8989F, -13.1069F, 5.5887F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r45 = group35.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8411F, -12.9335F, 5.5887F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r46 = group35.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3786F, -12.4903F, 5.608F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r47 = group35.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0156F, 0.0092F, 0.0F, 0.1927F, 1.0213F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9354F, -11.1799F, 5.608F, 0.0F, 0.0F, -0.7854F));

		PartDefinition r = q.addOrReplaceChild("r", CubeListBuilder.create(), PartPose.offset(-7.4477F, 0.0F, 0.0F));

		PartDefinition group36 = r.addOrReplaceChild("group36", CubeListBuilder.create().texOffs(0, 0).addBox(10.9118F, -8.8598F, 2.7109F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.7068F, -8.8393F, 2.9774F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(12.1381F, -8.8393F, 2.9774F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(10.6658F, -8.8598F, 3.4899F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.1168F, -8.8598F, 2.5059F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.3218F, -8.8598F, 2.3009F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.9938F, -8.8393F, 4.5559F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.1989F, -8.8393F, 4.7609F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.7889F, -8.8393F, 4.3509F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group37 = r.addOrReplaceChild("group37", CubeListBuilder.create().texOffs(12, 64).addBox(9.6369F, -9.1483F, 2.7359F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(64, 66).addBox(9.4319F, -9.1278F, 3.0024F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(2, 67).addBox(10.8632F, -9.1278F, 3.0024F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(58, 62).addBox(9.3909F, -9.1483F, 3.5149F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8419F, -9.1483F, 2.5309F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.0469F, -9.1483F, 2.3259F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.7188F, -9.1278F, 4.5809F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9239F, -9.1278F, 4.7859F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5139F, -9.1278F, 4.3759F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(60, 50).addBox(10.1423F, -9.2508F, 2.2089F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r48 = group37.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3712F, -9.2508F, 4.9294F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group38 = group37.addOrReplaceChild("group38", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r49 = group38.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5052F, -8.9303F, 4.5649F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r50 = group38.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 4.4009F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r51 = group38.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5052F, -8.9303F, 4.0524F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r52 = group38.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 3.8884F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r53 = group38.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 3.4169F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r54 = group38.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5052F, -8.9303F, 3.5809F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r55 = group38.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5052F, -8.9303F, 3.1299F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r56 = group38.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.7307F, -8.9098F, 3.0274F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group39 = group37.addOrReplaceChild("group39", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r57 = group39.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 4.8109F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r58 = group39.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 4.6469F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r59 = group39.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 4.2984F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r60 = group39.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 4.1344F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r61 = group39.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 3.7244F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r62 = group39.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 3.8884F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r63 = group39.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2833F, -8.9303F, 3.3144F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r64 = group39.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0578F, -8.9098F, 3.2119F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group40 = group37.addOrReplaceChild("group40", CubeListBuilder.create().texOffs(30, 65).addBox(9.4732F, -10.2213F, 3.5899F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.2283F, -10.2213F, 4.9489F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(28, 65).addBox(11.3423F, -10.2213F, 3.5899F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r65 = group40.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(36, 60).addBox(-0.1885F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5474F, -10.2008F, 4.3689F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r66 = group40.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(64, 31).addBox(-0.0165F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4732F, -10.2008F, 4.3689F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r67 = group40.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(52, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9446F, -10.2008F, 2.9749F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r68 = group40.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(66, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4367F, -10.2213F, 2.5034F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r69 = group40.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4982F, -10.2213F, 2.3189F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r70 = group40.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5391F, -10.2008F, 2.0934F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r71 = group40.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6007F, -10.2213F, 2.2164F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r72 = group40.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5223F, -10.2213F, 2.3189F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r73 = group40.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(0, 66).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5839F, -10.2213F, 2.5034F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r74 = group40.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(50, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0759F, -10.2008F, 2.9749F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group41 = group37.addOrReplaceChild("group41", CubeListBuilder.create().texOffs(66, 44).addBox(9.608F, -10.1656F, 3.5966F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.265F, -10.1656F, 4.7789F, 0.4816F, 0.1784F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(66, 41).addBox(11.2342F, -10.1656F, 3.5966F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r75 = group41.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(46, 64).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4125F, -10.1478F, 4.2743F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r76 = group41.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(44, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.608F, -10.1478F, 4.2743F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r77 = group41.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(48, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0182F, -10.1478F, 3.0615F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r78 = group41.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(66, 52).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4462F, -10.1656F, 2.6513F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r79 = group41.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4997F, -10.1656F, 2.4908F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r80 = group41.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1784F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5354F, -10.1478F, 2.2946F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r81 = group41.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1784F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5889F, -10.1656F, 2.4016F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r82 = group41.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5208F, -10.1656F, 2.4908F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r83 = group41.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(58, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5743F, -10.1656F, 2.6513F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r84 = group41.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(66, 47).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0023F, -10.1478F, 3.0615F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group42 = r.addOrReplaceChild("group42", CubeListBuilder.create().texOffs(16, 64).addBox(9.6743F, -8.9999F, 2.7857F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.4816F, -8.9806F, 3.0362F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.827F, -8.9806F, 3.0362F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(28, 63).addBox(9.443F, -8.9999F, 3.518F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.867F, -8.9999F, 2.593F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.0597F, -8.9999F, 2.4003F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.7513F, -8.9806F, 4.52F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.944F, -8.9806F, 4.7127F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5586F, -8.9806F, 4.3273F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9952F, -9.0577F, 4.7955F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.1494F, -9.0577F, 4.6028F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 1).addBox(9.2854F, -9.0577F, 3.518F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(2, 66).addBox(11.0424F, -9.0577F, 3.518F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r85 = group42.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(38, 64).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2351F, -9.0384F, 4.2502F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r86 = group42.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(66, 38).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.7919F, -9.0384F, 2.9399F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r87 = group42.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2716F, -9.0577F, 2.3232F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r88 = group42.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(66, 31).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3294F, -9.0577F, 2.4967F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r89 = group42.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3452F, -9.0577F, 2.2269F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r90 = group42.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.289F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2874F, -9.0384F, 2.1113F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r91 = group42.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2489F, -9.0577F, 2.3232F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r92 = group42.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(36, 66).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1911F, -9.0577F, 2.4967F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r93 = group42.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(66, 35).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7286F, -9.0384F, 2.9399F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r94 = group42.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(36, 64).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2854F, -9.0384F, 4.2502F, 0.0F, 0.7854F, 0.0F));

		PartDefinition t = q.addOrReplaceChild("t", CubeListBuilder.create(), PartPose.offset(-7.4477F, 0.0F, 0.0F));

		PartDefinition group43 = t.addOrReplaceChild("group43", CubeListBuilder.create().texOffs(0, 0).addBox(7.8868F, -6.0598F, 2.4359F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6818F, -6.0393F, 2.7024F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.1131F, -6.0393F, 2.7024F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(7.6408F, -6.0598F, 3.2149F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.0918F, -6.0598F, 2.2309F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.2968F, -6.0598F, 2.0259F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9688F, -6.0393F, 4.2809F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1738F, -6.0393F, 4.4859F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7638F, -6.0393F, 4.0759F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group44 = t.addOrReplaceChild("group44", CubeListBuilder.create().texOffs(64, 10).addBox(6.6119F, -6.3483F, 2.4609F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(62, 66).addBox(6.4069F, -6.3278F, 2.7274F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 67).addBox(7.8382F, -6.3278F, 2.7274F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(62, 58).addBox(6.3659F, -6.3483F, 3.2399F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.8169F, -6.3483F, 2.2559F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.0219F, -6.3483F, 2.0509F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.6939F, -6.3278F, 4.3059F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.8989F, -6.3278F, 4.5109F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.4889F, -6.3278F, 4.1009F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(6, 61).addBox(7.1173F, -6.4508F, 1.9339F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r95 = group44.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3462F, -6.4508F, 4.6544F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group45 = group44.addOrReplaceChild("group45", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r96 = group45.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4801F, -6.1303F, 4.2899F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r97 = group45.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 4.1259F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r98 = group45.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4801F, -6.1303F, 3.7774F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r99 = group45.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 3.6134F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r100 = group45.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 3.1419F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r101 = group45.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4801F, -6.1303F, 3.3059F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r102 = group45.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4801F, -6.1303F, 2.8549F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r103 = group45.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7056F, -6.1098F, 2.7524F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group46 = group44.addOrReplaceChild("group46", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r104 = group46.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 4.5359F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r105 = group46.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 4.3719F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r106 = group46.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 4.0234F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r107 = group46.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 3.8594F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r108 = group46.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 3.4494F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r109 = group46.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 3.6134F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r110 = group46.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2584F, -6.1303F, 3.0394F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r111 = group46.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0328F, -6.1098F, 2.9369F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group47 = group44.addOrReplaceChild("group47", CubeListBuilder.create().texOffs(32, 65).addBox(6.4482F, -7.4213F, 3.3149F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2033F, -7.4213F, 4.6739F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(34, 65).addBox(8.3174F, -7.4213F, 3.3149F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r112 = group47.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(46, 58).addBox(-0.1884F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5223F, -7.4008F, 4.0939F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r113 = group47.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(32, 16).addBox(-0.0165F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4482F, -7.4008F, 4.0939F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r114 = group47.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(48, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9197F, -7.4008F, 2.6999F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r115 = group47.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(62, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4116F, -7.4213F, 2.2284F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r116 = group47.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4732F, -7.4213F, 2.0439F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r117 = group47.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5142F, -7.4008F, 1.8184F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r118 = group47.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5757F, -7.4213F, 1.9414F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r119 = group47.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4974F, -7.4213F, 2.0439F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r120 = group47.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(60, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5589F, -7.4213F, 2.2284F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r121 = group47.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(46, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0509F, -7.4008F, 2.6999F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group48 = group44.addOrReplaceChild("group48", CubeListBuilder.create().texOffs(66, 43).addBox(6.583F, -7.3656F, 3.3216F, 0.1784F, 0.1783F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.24F, -7.3656F, 4.5039F, 0.4816F, 0.1783F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(44, 66).addBox(8.2092F, -7.3656F, 3.3216F, 0.1784F, 0.1783F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r122 = group48.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(52, 64).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1783F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3875F, -7.3478F, 3.9993F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r123 = group48.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(50, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1783F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.583F, -7.3478F, 3.9993F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r124 = group48.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(66, 49).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9932F, -7.3478F, 2.7865F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r125 = group48.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(56, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4212F, -7.3656F, 2.3763F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r126 = group48.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4747F, -7.3656F, 2.2158F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r127 = group48.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1783F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5104F, -7.3478F, 2.0196F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r128 = group48.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1783F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5639F, -7.3656F, 2.1266F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r129 = group48.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4958F, -7.3656F, 2.2158F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r130 = group48.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(54, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5493F, -7.3656F, 2.3763F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r131 = group48.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(66, 48).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9773F, -7.3478F, 2.7865F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group49 = t.addOrReplaceChild("group49", CubeListBuilder.create().texOffs(64, 26).addBox(6.6493F, -6.1998F, 2.5107F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.4566F, -6.1806F, 2.7612F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.802F, -6.1806F, 2.7612F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(58, 63).addBox(6.418F, -6.1998F, 3.243F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.842F, -6.1998F, 2.318F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.0347F, -6.1998F, 2.1253F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.7263F, -6.1806F, 4.245F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.919F, -6.1806F, 4.4377F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.5336F, -6.1806F, 4.0523F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.9702F, -6.2577F, 4.5205F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.1244F, -6.2577F, 4.3277F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 13).addBox(6.2604F, -6.2577F, 3.243F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(66, 14).addBox(8.0174F, -6.2577F, 3.243F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r132 = group49.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(64, 33).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2101F, -6.2384F, 3.9752F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r133 = group49.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(66, 30).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.7669F, -6.2384F, 2.6649F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r134 = group49.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2466F, -6.2577F, 2.0482F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r135 = group49.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(32, 66).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3044F, -6.2577F, 2.2217F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r136 = group49.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3202F, -6.2577F, 1.9519F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r137 = group49.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.2891F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2624F, -6.2384F, 1.8363F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r138 = group49.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2239F, -6.2577F, 2.0482F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r139 = group49.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(66, 34).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1661F, -6.2577F, 2.2217F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r140 = group49.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(66, 33).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.7036F, -6.2384F, 2.6649F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r141 = group49.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(64, 32).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2604F, -6.2384F, 3.9752F, 0.0F, 0.7854F, 0.0F));

		PartDefinition y = q.addOrReplaceChild("y", CubeListBuilder.create(), PartPose.offset(-7.4477F, 0.0F, 0.0F));

		PartDefinition group50 = y.addOrReplaceChild("group50", CubeListBuilder.create().texOffs(0, 0).addBox(8.6119F, -1.3348F, 2.6609F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4068F, -1.3143F, 2.9274F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8381F, -1.3143F, 2.9274F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(8.3659F, -1.3348F, 3.4399F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8168F, -1.3348F, 2.4559F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.0218F, -1.3348F, 2.2509F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6938F, -1.3143F, 4.5059F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8988F, -1.3143F, 4.7109F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4888F, -1.3143F, 4.3009F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group51 = y.addOrReplaceChild("group51", CubeListBuilder.create().texOffs(64, 11).addBox(7.3369F, -1.6233F, 2.6859F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(60, 66).addBox(7.1319F, -1.6028F, 2.9524F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(66, 66).addBox(8.5632F, -1.6028F, 2.9524F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(62, 59).addBox(7.0909F, -1.6233F, 3.4649F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.5419F, -1.6233F, 2.4809F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7469F, -1.6233F, 2.2759F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.4189F, -1.6028F, 4.5309F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6239F, -1.6028F, 4.7359F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2139F, -1.6028F, 4.3259F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 61).addBox(7.8423F, -1.7258F, 2.1589F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r142 = group51.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0711F, -1.7258F, 4.8794F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group52 = group51.addOrReplaceChild("group52", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r143 = group52.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 4.5149F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r144 = group52.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 4.3509F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r145 = group52.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 4.0024F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r146 = group52.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 3.8384F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r147 = group52.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 3.3669F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r148 = group52.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 3.5309F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r149 = group52.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 3.0799F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r150 = group52.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4306F, -1.3848F, 2.9774F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group53 = group51.addOrReplaceChild("group53", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r151 = group53.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 4.7609F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r152 = group53.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 4.5969F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r153 = group53.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 4.2484F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r154 = group53.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 4.0844F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r155 = group53.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 3.6744F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r156 = group53.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 3.8384F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r157 = group53.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9833F, -1.4053F, 3.2644F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r158 = group53.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7578F, -1.3848F, 3.1619F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group54 = group51.addOrReplaceChild("group54", CubeListBuilder.create().texOffs(38, 65).addBox(7.1732F, -2.6963F, 3.5399F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9283F, -2.6963F, 4.8989F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(36, 65).addBox(9.0424F, -2.6963F, 3.5399F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r159 = group54.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(20, 62).addBox(-0.1885F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2474F, -2.6758F, 4.3189F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r160 = group54.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(64, 30).addBox(-0.0166F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1732F, -2.6758F, 4.3189F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r161 = group54.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(56, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.6447F, -2.6758F, 2.9249F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r162 = group54.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(44, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1366F, -2.6963F, 2.4534F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r163 = group54.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1982F, -2.6963F, 2.2689F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r164 = group54.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2392F, -2.6758F, 2.0434F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r165 = group54.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3007F, -2.6963F, 2.1664F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r166 = group54.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2224F, -2.6963F, 2.2689F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r167 = group54.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(42, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2839F, -2.6963F, 2.4534F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r168 = group54.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(54, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7759F, -2.6758F, 2.9249F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group55 = group51.addOrReplaceChild("group55", CubeListBuilder.create().texOffs(42, 66).addBox(7.308F, -2.6406F, 3.5466F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.965F, -2.6406F, 4.7289F, 0.4816F, 0.1784F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(66, 42).addBox(8.9342F, -2.6406F, 3.5466F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r169 = group55.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(64, 44).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1125F, -2.6228F, 4.2243F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r170 = group55.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(48, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.308F, -2.6228F, 4.2243F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r171 = group55.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(66, 50).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.7182F, -2.6228F, 3.0115F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r172 = group55.addOrReplaceChild("cube_r172", CubeListBuilder.create().texOffs(52, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1462F, -2.6406F, 2.6013F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r173 = group55.addOrReplaceChild("cube_r173", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1997F, -2.6406F, 2.4408F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r174 = group55.addOrReplaceChild("cube_r174", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1784F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2354F, -2.6228F, 2.2446F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r175 = group55.addOrReplaceChild("cube_r175", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1784F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2889F, -2.6406F, 2.3516F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r176 = group55.addOrReplaceChild("cube_r176", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2208F, -2.6406F, 2.4408F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r177 = group55.addOrReplaceChild("cube_r177", CubeListBuilder.create().texOffs(66, 51).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2743F, -2.6406F, 2.6013F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r178 = group55.addOrReplaceChild("cube_r178", CubeListBuilder.create().texOffs(50, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7023F, -2.6228F, 3.0115F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group56 = y.addOrReplaceChild("group56", CubeListBuilder.create().texOffs(64, 27).addBox(7.3743F, -1.4748F, 2.7357F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.1816F, -1.4556F, 2.9862F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.527F, -1.4556F, 2.9862F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(48, 63).addBox(7.143F, -1.4748F, 3.468F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.567F, -1.4748F, 2.543F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7597F, -1.4748F, 2.3503F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.4513F, -1.4556F, 4.47F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.644F, -1.4556F, 4.6627F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2586F, -1.4556F, 4.2773F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6952F, -1.5327F, 4.7454F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.8494F, -1.5327F, 4.5527F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 2).addBox(6.9854F, -1.5327F, 3.468F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(66, 3).addBox(8.7424F, -1.5327F, 3.468F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r179 = group56.addOrReplaceChild("cube_r179", CubeListBuilder.create().texOffs(64, 35).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9351F, -1.5134F, 4.2002F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r180 = group56.addOrReplaceChild("cube_r180", CubeListBuilder.create().texOffs(66, 32).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4919F, -1.5134F, 2.8899F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r181 = group56.addOrReplaceChild("cube_r181", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9716F, -1.5327F, 2.2732F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r182 = group56.addOrReplaceChild("cube_r182", CubeListBuilder.create().texOffs(34, 66).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0294F, -1.5327F, 2.4467F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r183 = group56.addOrReplaceChild("cube_r183", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0452F, -1.5327F, 2.1769F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r184 = group56.addOrReplaceChild("cube_r184", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.289F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9874F, -1.5134F, 2.0613F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r185 = group56.addOrReplaceChild("cube_r185", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9489F, -1.5327F, 2.2732F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r186 = group56.addOrReplaceChild("cube_r186", CubeListBuilder.create().texOffs(66, 25).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8911F, -1.5327F, 2.4467F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r187 = group56.addOrReplaceChild("cube_r187", CubeListBuilder.create().texOffs(38, 66).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4286F, -1.5134F, 2.8899F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r188 = group56.addOrReplaceChild("cube_r188", CubeListBuilder.create().texOffs(64, 34).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9854F, -1.5134F, 4.2002F, 0.0F, 0.7854F, 0.0F));

		PartDefinition w = liebe.addOrReplaceChild("w", CubeListBuilder.create(), PartPose.offset(5.0F, 14.0F, 0.0F));

		PartDefinition u = w.addOrReplaceChild("u", CubeListBuilder.create().texOffs(64, 62).addBox(-0.425F, -2.1F, -0.375F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(16, 65).addBox(-5.475F, 4.175F, -0.275F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.525F, 2.125F, 4.825F, 0.425F, 0.5F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(46, 66).addBox(-0.525F, 0.0F, 4.925F, 0.5F, 0.55F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(60, 64).addBox(-0.875F, -2.3F, 4.575F, 0.5F, 1.125F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(66, 15).addBox(-5.75F, -2.25F, 4.625F, 0.5F, 0.775F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(62, 64).addBox(-5.75F, -4.75F, 4.475F, 0.5F, 0.775F, 0.45F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r189 = u.addOrReplaceChild("cube_r189", CubeListBuilder.create().texOffs(34, 19).addBox(-0.5F, -0.25F, 0.0F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.775F, 0.0F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r190 = u.addOrReplaceChild("cube_r190", CubeListBuilder.create().texOffs(28, 45).addBox(-0.5F, -0.25F, -5.5F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 2.375F, 5.2F, 0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r191 = u.addOrReplaceChild("cube_r191", CubeListBuilder.create().texOffs(14, 57).addBox(-5.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.125F, 0.3F, 5.3F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r192 = u.addOrReplaceChild("cube_r192", CubeListBuilder.create().texOffs(48, 57).addBox(-4.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 6.1F, 0.125F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r193 = u.addOrReplaceChild("cube_r193", CubeListBuilder.create().texOffs(0, 57).addBox(-4.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -2.2F, 0.125F, 0.0F, 0.0F, 0.3927F));

		PartDefinition i = w.addOrReplaceChild("i", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, 2.3556F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 6.0736F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 4.2036F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r194 = i.addOrReplaceChild("cube_r194", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r195 = i.addOrReplaceChild("cube_r195", CubeListBuilder.create().texOffs(0, 52).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r196 = i.addOrReplaceChild("cube_r196", CubeListBuilder.create().texOffs(48, 18).addBox(2.068F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		PartDefinition o = w.addOrReplaceChild("o", CubeListBuilder.create().texOffs(24, 65).addBox(-5.8F, -0.25F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(64, 64).addBox(-5.7F, 6.025F, 4.5F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 3.975F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(66, 23).addBox(-0.5F, 1.85F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r197 = o.addOrReplaceChild("cube_r197", CubeListBuilder.create().texOffs(58, 18).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r198 = o.addOrReplaceChild("cube_r198", CubeListBuilder.create().texOffs(58, 55).addBox(-0.5715F, -5.0276F, 2.05F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r199 = o.addOrReplaceChild("cube_r199", CubeListBuilder.create().texOffs(40, 26).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r200 = o.addOrReplaceChild("cube_r200", CubeListBuilder.create().texOffs(0, 38).addBox(-2.85F, -11.0052F, 1.4525F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition a = w.addOrReplaceChild("a", CubeListBuilder.create().texOffs(8, 65).addBox(-5.8F, -2.775F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(20, 65).addBox(-5.7F, 3.5F, 4.5F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 1.45F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(28, 66).addBox(-0.5F, -0.675F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r201 = a.addOrReplaceChild("cube_r201", CubeListBuilder.create().texOffs(58, 53).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.15F, 6.25F, 7.4F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r202 = a.addOrReplaceChild("cube_r202", CubeListBuilder.create().texOffs(58, 20).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r203 = a.addOrReplaceChild("cube_r203", CubeListBuilder.create().texOffs(0, 59).addBox(-0.5715F, -5.0276F, 2.05F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r204 = a.addOrReplaceChild("cube_r204", CubeListBuilder.create().texOffs(40, 40).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r205 = a.addOrReplaceChild("cube_r205", CubeListBuilder.create().texOffs(40, 33).addBox(-2.85F, -3.3369F, -1.7238F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F))
		.texOffs(48, 0).addBox(-2.85F, -11.0052F, 1.4525F, 0.5F, 0.5F, 5.35F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition s = w.addOrReplaceChild("s", CubeListBuilder.create().texOffs(12, 65).addBox(-5.8F, 1.575F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 5.8F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(30, 66).addBox(-0.5F, 3.675F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r206 = s.addOrReplaceChild("cube_r206", CubeListBuilder.create().texOffs(54, 45).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r207 = s.addOrReplaceChild("cube_r207", CubeListBuilder.create().texOffs(14, 45).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r208 = s.addOrReplaceChild("cube_r208", CubeListBuilder.create().texOffs(0, 45).addBox(-2.85F, -11.0051F, 1.4525F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition d = w.addOrReplaceChild("d", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, 0.1556F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.9715F, 5.6776F, 4.3822F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 3.8736F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 2.0036F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r209 = d.addOrReplaceChild("cube_r209", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r210 = d.addOrReplaceChild("cube_r210", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r211 = d.addOrReplaceChild("cube_r211", CubeListBuilder.create().texOffs(50, 48).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r212 = d.addOrReplaceChild("cube_r212", CubeListBuilder.create().texOffs(40, 48).addBox(2.068F, -2.9365F, -1.5169F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F))
		.texOffs(14, 38).addBox(2.068F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		PartDefinition f = w.addOrReplaceChild("f", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, -2.4944F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.9715F, 3.0276F, 4.3822F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 1.2236F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, -0.6464F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r213 = f.addOrReplaceChild("cube_r213", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r214 = f.addOrReplaceChild("cube_r214", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 3.8876F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r215 = f.addOrReplaceChild("cube_r215", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r216 = f.addOrReplaceChild("cube_r216", CubeListBuilder.create().texOffs(10, 52).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r217 = f.addOrReplaceChild("cube_r217", CubeListBuilder.create().texOffs(28, 51).addBox(2.068F, -2.9365F, -1.5169F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F))
		.texOffs(38, 53).addBox(2.043F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		PartDefinition liebe2 = partdefinition.addOrReplaceChild("liebe2", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition qw = liebe2.addOrReplaceChild("qw", CubeListBuilder.create(), PartPose.offset(5.0F, 14.0F, 0.0F));

		PartDefinition qe = qw.addOrReplaceChild("qe", CubeListBuilder.create(), PartPose.offset(-7.4478F, 0.0F, 0.0F));

		PartDefinition group2 = qe.addOrReplaceChild("group2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(9.5618F, -12.3443F, 5.4116F, 1.23F, 0.779F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.3568F, -12.0778F, 5.4321F, 0.205F, 0.5125F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(10.7881F, -12.0778F, 5.4321F, 0.205F, 0.5125F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.3158F, -11.5653F, 5.4116F, 1.722F, 0.861F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.7668F, -12.5493F, 5.4116F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.9718F, -12.7543F, 5.4116F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.6438F, -10.4993F, 5.4321F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.8488F, -10.2943F, 5.4321F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(9.4388F, -10.7043F, 5.4321F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group3 = qe.addOrReplaceChild("group3", CubeListBuilder.create().texOffs(64, 43).addBox(8.2869F, -12.6943F, 5.4981F, 1.23F, 0.779F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.0819F, -12.4278F, 5.5186F, 0.205F, 0.5125F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5132F, -12.4278F, 5.5186F, 0.205F, 0.5125F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(64, 12).addBox(8.0409F, -11.9153F, 5.4981F, 1.722F, 0.861F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4919F, -12.8993F, 5.4981F, 0.82F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6969F, -13.1043F, 5.4981F, 0.41F, 0.205F, 0.1435F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.3689F, -10.8493F, 5.5186F, 1.066F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.5739F, -10.6443F, 5.5186F, 0.6765F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1639F, -11.0543F, 5.5186F, 1.476F, 0.205F, 0.0615F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7923F, -13.2213F, 5.3956F, 0.2255F, 2.665F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r218 = group3.addOrReplaceChild("cube_r218", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0675F, 0.0F, 0.2255F, 0.901F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0212F, -10.5008F, 5.3956F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group4 = group3.addOrReplaceChild("group4", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r219 = group4.addOrReplaceChild("cube_r219", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -10.5903F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r220 = group4.addOrReplaceChild("cube_r220", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -10.7543F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r221 = group4.addOrReplaceChild("cube_r221", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -11.1028F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r222 = group4.addOrReplaceChild("cube_r222", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -11.2668F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r223 = group4.addOrReplaceChild("cube_r223", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.3895F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5446F, -11.7383F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r224 = group4.addOrReplaceChild("cube_r224", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -11.5743F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r225 = group4.addOrReplaceChild("cube_r225", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.2665F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1551F, -12.0253F, 5.4411F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r226 = group4.addOrReplaceChild("cube_r226", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, -0.1263F, 0.0F, 0.287F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3806F, -12.1278F, 5.4616F, 0.0F, 0.0F, -0.7854F));

		PartDefinition group5 = group3.addOrReplaceChild("group5", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r227 = group5.addOrReplaceChild("cube_r227", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0768F, -10.3443F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r228 = group5.addOrReplaceChild("cube_r228", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -10.5083F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r229 = group5.addOrReplaceChild("cube_r229", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0768F, -10.8568F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r230 = group5.addOrReplaceChild("cube_r230", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, -0.1263F, 0.0F, 0.6765F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -11.0208F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r231 = group5.addOrReplaceChild("cube_r231", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, -0.1263F, 0.0F, 0.5535F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6873F, -11.4308F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r232 = group5.addOrReplaceChild("cube_r232", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, -0.1263F, 0.0F, 0.4305F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0768F, -11.2668F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r233 = group5.addOrReplaceChild("cube_r233", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, -0.1263F, 0.0F, 0.3075F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9333F, -11.8408F, 5.4411F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r234 = group5.addOrReplaceChild("cube_r234", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, -0.1263F, 0.0F, 0.287F, 0.123F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7078F, -11.9433F, 5.4616F, 0.0F, 0.0F, 0.7854F));

		PartDefinition group6 = group3.addOrReplaceChild("group6", CubeListBuilder.create().texOffs(0, 0).addBox(8.1232F, -12.8653F, 5.4501F, 0.205F, 0.7995F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8783F, -11.5063F, 5.4501F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9924F, -12.8653F, 5.4501F, 0.205F, 0.7995F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r235 = group6.addOrReplaceChild("cube_r235", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1884F, 0.0098F, 0.0F, 0.205F, 1.0865F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1974F, -12.0863F, 5.4706F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r236 = group6.addOrReplaceChild("cube_r236", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0166F, 0.0098F, 0.0F, 0.205F, 1.0865F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1232F, -12.0863F, 5.4706F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r237 = group6.addOrReplaceChild("cube_r237", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5947F, -13.4803F, 5.4706F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r238 = group6.addOrReplaceChild("cube_r238", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0867F, -13.9518F, 5.4501F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r239 = group6.addOrReplaceChild("cube_r239", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1482F, -14.1363F, 5.4501F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r240 = group6.addOrReplaceChild("cube_r240", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, -0.0648F, 0.0F, 0.287F, 0.3075F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1892F, -14.3618F, 5.4706F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r241 = group6.addOrReplaceChild("cube_r241", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, -0.0033F, 0.0F, 0.205F, 0.2255F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2507F, -14.2388F, 5.4501F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r242 = group6.addOrReplaceChild("cube_r242", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1723F, -14.1363F, 5.4501F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r243 = group6.addOrReplaceChild("cube_r243", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2339F, -13.9518F, 5.4501F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r244 = group6.addOrReplaceChild("cube_r244", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.7585F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7259F, -13.4803F, 5.4706F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group7 = group3.addOrReplaceChild("group7", CubeListBuilder.create().texOffs(0, 0).addBox(8.258F, -12.8586F, 5.5058F, 0.1784F, 0.6956F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.915F, -11.6762F, 5.5058F, 0.4816F, 0.1783F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8842F, -12.8586F, 5.5058F, 0.1784F, 0.6956F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r245 = group7.addOrReplaceChild("cube_r245", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1639F, 0.0085F, 0.0F, 0.1784F, 0.9453F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0625F, -12.1808F, 5.5236F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r246 = group7.addOrReplaceChild("cube_r246", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0144F, 0.0085F, 0.0F, 0.1784F, 0.9453F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.258F, -12.1808F, 5.5236F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r247 = group7.addOrReplaceChild("cube_r247", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6682F, -13.3936F, 5.5236F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r248 = group7.addOrReplaceChild("cube_r248", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0962F, -13.8038F, 5.5058F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r249 = group7.addOrReplaceChild("cube_r249", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1497F, -13.9643F, 5.5058F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r250 = group7.addOrReplaceChild("cube_r250", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, -0.0564F, 0.0F, 0.2497F, 0.2675F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1854F, -14.1605F, 5.5236F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r251 = group7.addOrReplaceChild("cube_r251", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, -0.0029F, 0.0F, 0.1784F, 0.1962F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2389F, -14.0535F, 5.5058F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r252 = group7.addOrReplaceChild("cube_r252", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1708F, -13.9643F, 5.5058F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r253 = group7.addOrReplaceChild("cube_r253", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2243F, -13.8038F, 5.5058F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r254 = group7.addOrReplaceChild("cube_r254", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.6599F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.6523F, -13.3936F, 5.5236F, 0.0F, 0.0F, -0.3927F));

		PartDefinition group8 = qe.addOrReplaceChild("group8", CubeListBuilder.create().texOffs(40, 64).addBox(8.3243F, -12.6444F, 5.6465F, 1.1562F, 0.7323F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1316F, -12.3939F, 5.6658F, 0.1927F, 0.4817F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.477F, -12.3939F, 5.6658F, 0.1927F, 0.4817F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(28, 64).addBox(8.093F, -11.9122F, 5.6465F, 1.6187F, 0.8093F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.517F, -12.8371F, 5.6465F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7096F, -13.0298F, 5.6465F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4013F, -10.9101F, 5.6658F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.594F, -10.7174F, 5.6658F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.2086F, -11.1028F, 5.6658F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6452F, -10.6347F, 5.5887F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.7994F, -10.8274F, 5.5887F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9354F, -11.9122F, 5.5887F, 0.1927F, 0.7515F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.6924F, -11.9122F, 5.5887F, 0.1927F, 0.7515F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r255 = group8.addOrReplaceChild("cube_r255", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1771F, 0.0092F, 0.0F, 0.1927F, 1.0213F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.8851F, -11.1799F, 5.608F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r256 = group8.addOrReplaceChild("cube_r256", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4419F, -12.4903F, 5.608F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r257 = group8.addOrReplaceChild("cube_r257", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9216F, -13.1069F, 5.5887F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r258 = group8.addOrReplaceChild("cube_r258", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9794F, -12.9335F, 5.5887F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r259 = group8.addOrReplaceChild("cube_r259", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, -0.0031F, 0.0F, 0.1927F, 0.212F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9952F, -13.2033F, 5.5887F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r260 = group8.addOrReplaceChild("cube_r260", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, -0.061F, 0.0F, 0.2698F, 0.289F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9374F, -13.3189F, 5.608F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r261 = group8.addOrReplaceChild("cube_r261", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8989F, -13.1069F, 5.5887F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r262 = group8.addOrReplaceChild("cube_r262", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8411F, -12.9335F, 5.5887F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r263 = group8.addOrReplaceChild("cube_r263", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.713F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3786F, -12.4903F, 5.608F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r264 = group8.addOrReplaceChild("cube_r264", CubeListBuilder.create().texOffs(0, 0).addBox(-0.0156F, 0.0092F, 0.0F, 0.1927F, 1.0213F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9354F, -11.1799F, 5.608F, 0.0F, 0.0F, -0.7854F));

		PartDefinition we = qw.addOrReplaceChild("we", CubeListBuilder.create(), PartPose.offset(-7.4478F, 0.0F, 0.0F));

		PartDefinition group9 = we.addOrReplaceChild("group9", CubeListBuilder.create().texOffs(0, 0).addBox(10.9118F, -8.8598F, 2.7109F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.7068F, -8.8393F, 2.9774F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(12.1381F, -8.8393F, 2.9774F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(10.6658F, -8.8598F, 3.4899F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.1168F, -8.8598F, 2.5059F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.3218F, -8.8598F, 2.3009F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.9938F, -8.8393F, 4.5559F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(11.1988F, -8.8393F, 4.7609F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.7889F, -8.8393F, 4.3509F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group10 = we.addOrReplaceChild("group10", CubeListBuilder.create().texOffs(12, 64).addBox(9.6369F, -9.1483F, 2.7359F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(64, 66).addBox(9.4319F, -9.1278F, 3.0024F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(2, 67).addBox(10.8632F, -9.1278F, 3.0024F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(58, 62).addBox(9.3909F, -9.1483F, 3.5149F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8419F, -9.1483F, 2.5309F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.0469F, -9.1483F, 2.3259F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.7188F, -9.1278F, 4.5809F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9238F, -9.1278F, 4.7859F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5139F, -9.1278F, 4.3759F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(60, 50).addBox(10.1423F, -9.2508F, 2.2089F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r265 = group10.addOrReplaceChild("cube_r265", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3712F, -9.2508F, 4.9294F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group11 = group10.addOrReplaceChild("group11", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r266 = group11.addOrReplaceChild("cube_r266", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5051F, -8.9303F, 4.5649F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r267 = group11.addOrReplaceChild("cube_r267", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 4.4009F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r268 = group11.addOrReplaceChild("cube_r268", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5051F, -8.9303F, 4.0524F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r269 = group11.addOrReplaceChild("cube_r269", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 3.8884F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r270 = group11.addOrReplaceChild("cube_r270", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.8946F, -8.9098F, 3.4169F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r271 = group11.addOrReplaceChild("cube_r271", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5051F, -8.9303F, 3.5809F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r272 = group11.addOrReplaceChild("cube_r272", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5051F, -8.9303F, 3.1299F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r273 = group11.addOrReplaceChild("cube_r273", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.7306F, -8.9098F, 3.0274F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group12 = group10.addOrReplaceChild("group12", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r274 = group12.addOrReplaceChild("cube_r274", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 4.8109F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r275 = group12.addOrReplaceChild("cube_r275", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 4.6469F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r276 = group12.addOrReplaceChild("cube_r276", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 4.2984F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r277 = group12.addOrReplaceChild("cube_r277", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 4.1344F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r278 = group12.addOrReplaceChild("cube_r278", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0373F, -8.9098F, 3.7244F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r279 = group12.addOrReplaceChild("cube_r279", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4268F, -8.9303F, 3.8884F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r280 = group12.addOrReplaceChild("cube_r280", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2833F, -8.9303F, 3.3144F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r281 = group12.addOrReplaceChild("cube_r281", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0579F, -8.9098F, 3.2119F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group13 = group10.addOrReplaceChild("group13", CubeListBuilder.create().texOffs(30, 65).addBox(9.4732F, -10.2213F, 3.5899F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.2283F, -10.2213F, 4.9489F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(28, 65).addBox(11.3424F, -10.2213F, 3.5899F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r282 = group13.addOrReplaceChild("cube_r282", CubeListBuilder.create().texOffs(36, 60).addBox(-0.1884F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5473F, -10.2008F, 4.3689F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r283 = group13.addOrReplaceChild("cube_r283", CubeListBuilder.create().texOffs(64, 31).addBox(-0.0166F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4732F, -10.2008F, 4.3689F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r284 = group13.addOrReplaceChild("cube_r284", CubeListBuilder.create().texOffs(52, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9447F, -10.2008F, 2.9749F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r285 = group13.addOrReplaceChild("cube_r285", CubeListBuilder.create().texOffs(66, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4367F, -10.2213F, 2.5034F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r286 = group13.addOrReplaceChild("cube_r286", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4982F, -10.2213F, 2.3189F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r287 = group13.addOrReplaceChild("cube_r287", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5392F, -10.2008F, 2.0934F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r288 = group13.addOrReplaceChild("cube_r288", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6006F, -10.2213F, 2.2164F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r289 = group13.addOrReplaceChild("cube_r289", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5224F, -10.2213F, 2.3189F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r290 = group13.addOrReplaceChild("cube_r290", CubeListBuilder.create().texOffs(0, 66).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5838F, -10.2213F, 2.5034F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r291 = group13.addOrReplaceChild("cube_r291", CubeListBuilder.create().texOffs(50, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0759F, -10.2008F, 2.9749F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group14 = group10.addOrReplaceChild("group14", CubeListBuilder.create().texOffs(66, 44).addBox(9.608F, -10.1656F, 3.5966F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.265F, -10.1656F, 4.7789F, 0.4816F, 0.1784F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(66, 41).addBox(11.2342F, -10.1656F, 3.5966F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r292 = group14.addOrReplaceChild("cube_r292", CubeListBuilder.create().texOffs(46, 64).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.4125F, -10.1478F, 4.2743F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r293 = group14.addOrReplaceChild("cube_r293", CubeListBuilder.create().texOffs(44, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.608F, -10.1478F, 4.2743F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r294 = group14.addOrReplaceChild("cube_r294", CubeListBuilder.create().texOffs(48, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0182F, -10.1478F, 3.0615F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r295 = group14.addOrReplaceChild("cube_r295", CubeListBuilder.create().texOffs(66, 52).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4462F, -10.1656F, 2.6513F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r296 = group14.addOrReplaceChild("cube_r296", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4997F, -10.1656F, 2.4908F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r297 = group14.addOrReplaceChild("cube_r297", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1784F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5354F, -10.1478F, 2.2946F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r298 = group14.addOrReplaceChild("cube_r298", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1784F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5889F, -10.1656F, 2.4016F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r299 = group14.addOrReplaceChild("cube_r299", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5208F, -10.1656F, 2.4908F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r300 = group14.addOrReplaceChild("cube_r300", CubeListBuilder.create().texOffs(58, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5743F, -10.1656F, 2.6513F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r301 = group14.addOrReplaceChild("cube_r301", CubeListBuilder.create().texOffs(66, 47).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0023F, -10.1478F, 3.0615F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group15 = we.addOrReplaceChild("group15", CubeListBuilder.create().texOffs(16, 64).addBox(9.6743F, -8.9999F, 2.7857F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.4816F, -8.9806F, 3.0362F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.827F, -8.9806F, 3.0362F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(28, 63).addBox(9.443F, -8.9999F, 3.518F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.867F, -8.9999F, 2.593F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.0597F, -8.9999F, 2.4003F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.7513F, -8.9806F, 4.52F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.944F, -8.9806F, 4.7127F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.5586F, -8.9806F, 4.3273F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.9952F, -9.0577F, 4.7955F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(10.1494F, -9.0577F, 4.6028F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 1).addBox(9.2854F, -9.0577F, 3.518F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(2, 66).addBox(11.0424F, -9.0577F, 3.518F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r302 = group15.addOrReplaceChild("cube_r302", CubeListBuilder.create().texOffs(38, 64).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2351F, -9.0384F, 4.2502F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r303 = group15.addOrReplaceChild("cube_r303", CubeListBuilder.create().texOffs(66, 38).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.7919F, -9.0384F, 2.9399F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r304 = group15.addOrReplaceChild("cube_r304", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2716F, -9.0577F, 2.3232F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r305 = group15.addOrReplaceChild("cube_r305", CubeListBuilder.create().texOffs(66, 31).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3294F, -9.0577F, 2.4967F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r306 = group15.addOrReplaceChild("cube_r306", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3452F, -9.0577F, 2.2269F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r307 = group15.addOrReplaceChild("cube_r307", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.289F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2874F, -9.0384F, 2.1113F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r308 = group15.addOrReplaceChild("cube_r308", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2489F, -9.0577F, 2.3232F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r309 = group15.addOrReplaceChild("cube_r309", CubeListBuilder.create().texOffs(36, 66).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1911F, -9.0577F, 2.4967F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r310 = group15.addOrReplaceChild("cube_r310", CubeListBuilder.create().texOffs(66, 35).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7286F, -9.0384F, 2.9399F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r311 = group15.addOrReplaceChild("cube_r311", CubeListBuilder.create().texOffs(36, 64).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2854F, -9.0384F, 4.2502F, 0.0F, 0.7854F, 0.0F));

		PartDefinition wq = qw.addOrReplaceChild("wq", CubeListBuilder.create(), PartPose.offset(-7.4478F, 0.0F, 0.0F));

		PartDefinition group16 = wq.addOrReplaceChild("group16", CubeListBuilder.create().texOffs(0, 0).addBox(7.8868F, -6.0598F, 2.4359F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6818F, -6.0393F, 2.7024F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.1131F, -6.0393F, 2.7024F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(7.6409F, -6.0598F, 3.2149F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.0918F, -6.0598F, 2.2309F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.2968F, -6.0598F, 2.0259F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9688F, -6.0393F, 4.2809F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.1738F, -6.0393F, 4.4859F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7638F, -6.0393F, 4.0759F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group17 = wq.addOrReplaceChild("group17", CubeListBuilder.create().texOffs(64, 10).addBox(6.6118F, -6.3483F, 2.4609F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(62, 66).addBox(6.4068F, -6.3278F, 2.7274F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 67).addBox(7.8381F, -6.3278F, 2.7274F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(62, 58).addBox(6.3659F, -6.3483F, 3.2399F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.8168F, -6.3483F, 2.2559F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.0218F, -6.3483F, 2.0509F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.6938F, -6.3278F, 4.3059F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.8988F, -6.3278F, 4.5109F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.4888F, -6.3278F, 4.1009F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(6, 61).addBox(7.1173F, -6.4508F, 1.9339F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r312 = group17.addOrReplaceChild("cube_r312", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3462F, -6.4508F, 4.6544F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group18 = group17.addOrReplaceChild("group18", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r313 = group18.addOrReplaceChild("cube_r313", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4802F, -6.1303F, 4.2899F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r314 = group18.addOrReplaceChild("cube_r314", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 4.1259F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r315 = group18.addOrReplaceChild("cube_r315", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4802F, -6.1303F, 3.7774F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r316 = group18.addOrReplaceChild("cube_r316", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 3.6134F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r317 = group18.addOrReplaceChild("cube_r317", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8697F, -6.1098F, 3.1419F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r318 = group18.addOrReplaceChild("cube_r318", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4802F, -6.1303F, 3.3059F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r319 = group18.addOrReplaceChild("cube_r319", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4802F, -6.1303F, 2.8549F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r320 = group18.addOrReplaceChild("cube_r320", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7056F, -6.1098F, 2.7524F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group19 = group17.addOrReplaceChild("group19", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r321 = group19.addOrReplaceChild("cube_r321", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 4.5359F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r322 = group19.addOrReplaceChild("cube_r322", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 4.3719F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r323 = group19.addOrReplaceChild("cube_r323", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 4.0234F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r324 = group19.addOrReplaceChild("cube_r324", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 3.8594F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r325 = group19.addOrReplaceChild("cube_r325", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0123F, -6.1098F, 3.4494F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r326 = group19.addOrReplaceChild("cube_r326", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4018F, -6.1303F, 3.6134F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r327 = group19.addOrReplaceChild("cube_r327", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2584F, -6.1303F, 3.0394F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r328 = group19.addOrReplaceChild("cube_r328", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0328F, -6.1098F, 2.9369F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group20 = group17.addOrReplaceChild("group20", CubeListBuilder.create().texOffs(32, 65).addBox(6.4482F, -7.4213F, 3.3149F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2033F, -7.4213F, 4.6739F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(34, 65).addBox(8.3174F, -7.4213F, 3.3149F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r329 = group20.addOrReplaceChild("cube_r329", CubeListBuilder.create().texOffs(46, 58).addBox(-0.1885F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5224F, -7.4008F, 4.0939F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r330 = group20.addOrReplaceChild("cube_r330", CubeListBuilder.create().texOffs(32, 16).addBox(-0.0166F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4482F, -7.4008F, 4.0939F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r331 = group20.addOrReplaceChild("cube_r331", CubeListBuilder.create().texOffs(48, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9196F, -7.4008F, 2.6999F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r332 = group20.addOrReplaceChild("cube_r332", CubeListBuilder.create().texOffs(62, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4116F, -7.4213F, 2.2284F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r333 = group20.addOrReplaceChild("cube_r333", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4731F, -7.4213F, 2.0439F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r334 = group20.addOrReplaceChild("cube_r334", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5141F, -7.4008F, 1.8184F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r335 = group20.addOrReplaceChild("cube_r335", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5757F, -7.4213F, 1.9414F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r336 = group20.addOrReplaceChild("cube_r336", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4973F, -7.4213F, 2.0439F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r337 = group20.addOrReplaceChild("cube_r337", CubeListBuilder.create().texOffs(60, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5589F, -7.4213F, 2.2284F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r338 = group20.addOrReplaceChild("cube_r338", CubeListBuilder.create().texOffs(46, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0509F, -7.4008F, 2.6999F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group21 = group17.addOrReplaceChild("group21", CubeListBuilder.create().texOffs(66, 43).addBox(6.583F, -7.3656F, 3.3216F, 0.1784F, 0.1783F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.24F, -7.3656F, 4.5039F, 0.4815F, 0.1783F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(44, 66).addBox(8.2092F, -7.3656F, 3.3216F, 0.1784F, 0.1783F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r339 = group21.addOrReplaceChild("cube_r339", CubeListBuilder.create().texOffs(52, 64).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1783F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3875F, -7.3478F, 3.9993F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r340 = group21.addOrReplaceChild("cube_r340", CubeListBuilder.create().texOffs(50, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1783F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.583F, -7.3478F, 3.9993F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r341 = group21.addOrReplaceChild("cube_r341", CubeListBuilder.create().texOffs(66, 49).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9932F, -7.3478F, 2.7865F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r342 = group21.addOrReplaceChild("cube_r342", CubeListBuilder.create().texOffs(56, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4212F, -7.3656F, 2.3763F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r343 = group21.addOrReplaceChild("cube_r343", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4747F, -7.3656F, 2.2158F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r344 = group21.addOrReplaceChild("cube_r344", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1783F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5104F, -7.3478F, 2.0196F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r345 = group21.addOrReplaceChild("cube_r345", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1783F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5639F, -7.3656F, 2.1266F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r346 = group21.addOrReplaceChild("cube_r346", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4958F, -7.3656F, 2.2158F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r347 = group21.addOrReplaceChild("cube_r347", CubeListBuilder.create().texOffs(54, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5493F, -7.3656F, 2.3763F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r348 = group21.addOrReplaceChild("cube_r348", CubeListBuilder.create().texOffs(66, 48).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1783F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9773F, -7.3478F, 2.7865F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group22 = wq.addOrReplaceChild("group22", CubeListBuilder.create().texOffs(64, 26).addBox(6.6492F, -6.1998F, 2.5107F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.4566F, -6.1806F, 2.7612F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.802F, -6.1806F, 2.7612F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(58, 63).addBox(6.418F, -6.1998F, 3.243F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.8419F, -6.1998F, 2.318F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.0347F, -6.1998F, 2.1253F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.7263F, -6.1806F, 4.245F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.919F, -6.1806F, 4.4377F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.5336F, -6.1806F, 4.0523F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(6.9702F, -6.2577F, 4.5205F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.1244F, -6.2577F, 4.3277F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 13).addBox(6.2604F, -6.2577F, 3.243F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(66, 14).addBox(8.0174F, -6.2577F, 3.243F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r349 = group22.addOrReplaceChild("cube_r349", CubeListBuilder.create().texOffs(64, 33).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2101F, -6.2384F, 3.9752F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r350 = group22.addOrReplaceChild("cube_r350", CubeListBuilder.create().texOffs(66, 30).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.7669F, -6.2384F, 2.6649F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r351 = group22.addOrReplaceChild("cube_r351", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2466F, -6.2577F, 2.0482F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r352 = group22.addOrReplaceChild("cube_r352", CubeListBuilder.create().texOffs(32, 66).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3044F, -6.2577F, 2.2217F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r353 = group22.addOrReplaceChild("cube_r353", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3202F, -6.2577F, 1.9519F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r354 = group22.addOrReplaceChild("cube_r354", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.2891F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2624F, -6.2384F, 1.8363F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r355 = group22.addOrReplaceChild("cube_r355", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2239F, -6.2577F, 2.0482F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r356 = group22.addOrReplaceChild("cube_r356", CubeListBuilder.create().texOffs(66, 34).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1661F, -6.2577F, 2.2217F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r357 = group22.addOrReplaceChild("cube_r357", CubeListBuilder.create().texOffs(66, 33).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.7036F, -6.2384F, 2.6649F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r358 = group22.addOrReplaceChild("cube_r358", CubeListBuilder.create().texOffs(64, 32).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2604F, -6.2384F, 3.9752F, 0.0F, 0.7854F, 0.0F));

		PartDefinition eq = qw.addOrReplaceChild("eq", CubeListBuilder.create(), PartPose.offset(-7.4478F, 0.0F, 0.0F));

		PartDefinition group23 = eq.addOrReplaceChild("group23", CubeListBuilder.create().texOffs(0, 0).addBox(8.6118F, -1.3348F, 2.6609F, 1.23F, 0.205F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4069F, -1.3143F, 2.9274F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.8381F, -1.3143F, 2.9274F, 0.205F, 0.205F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(8.3659F, -1.3348F, 3.4399F, 1.722F, 0.205F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8168F, -1.3348F, 2.4559F, 0.82F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(9.0218F, -1.3348F, 2.2509F, 0.41F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.6938F, -1.3143F, 4.5059F, 1.066F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.8988F, -1.3143F, 4.7109F, 0.6765F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.4888F, -1.3143F, 4.3009F, 1.476F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -5.25F, -7.975F));

		PartDefinition group24 = eq.addOrReplaceChild("group24", CubeListBuilder.create().texOffs(64, 11).addBox(7.3368F, -1.6233F, 2.6859F, 1.23F, 0.0615F, 0.779F, new CubeDeformation(0.0F))
		.texOffs(60, 66).addBox(7.1319F, -1.6028F, 2.9524F, 0.205F, 0.1435F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(66, 66).addBox(8.5632F, -1.6028F, 2.9524F, 0.205F, 0.0615F, 0.5125F, new CubeDeformation(0.0F))
		.texOffs(62, 59).addBox(7.0908F, -1.6233F, 3.4649F, 1.722F, 0.1435F, 0.861F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.5418F, -1.6233F, 2.4809F, 0.82F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7468F, -1.6233F, 2.2759F, 0.41F, 0.1435F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.4188F, -1.6028F, 4.5309F, 1.066F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6239F, -1.6028F, 4.7359F, 0.6765F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2138F, -1.6028F, 4.3259F, 1.476F, 0.0615F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(0, 61).addBox(7.8423F, -1.7258F, 2.1589F, 0.2255F, 0.205F, 2.665F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r359 = group24.addOrReplaceChild("cube_r359", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2083F, 0.0F, 0.0675F, 0.2255F, 0.205F, 0.451F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0712F, -1.7258F, 4.8794F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group25 = group24.addOrReplaceChild("group25", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r360 = group25.addOrReplaceChild("cube_r360", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 4.5149F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r361 = group25.addOrReplaceChild("cube_r361", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 4.3509F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r362 = group25.addOrReplaceChild("cube_r362", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 4.0024F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r363 = group25.addOrReplaceChild("cube_r363", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 3.8384F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r364 = group25.addOrReplaceChild("cube_r364", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.3895F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5946F, -1.3848F, 3.3669F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r365 = group25.addOrReplaceChild("cube_r365", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 3.5309F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r366 = group25.addOrReplaceChild("cube_r366", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.2665F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2051F, -1.4053F, 3.0799F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r367 = group25.addOrReplaceChild("cube_r367", CubeListBuilder.create().texOffs(0, 0).addBox(-0.006F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4306F, -1.3848F, 2.9774F, 0.0F, 0.7854F, 0.0F));

		PartDefinition group26 = group24.addOrReplaceChild("group26", CubeListBuilder.create(), PartPose.offset(-1.1545F, -0.3F, -0.025F));

		PartDefinition cube_r368 = group26.addOrReplaceChild("cube_r368", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 4.7609F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r369 = group26.addOrReplaceChild("cube_r369", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 4.5969F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r370 = group26.addOrReplaceChild("cube_r370", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 4.2484F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r371 = group26.addOrReplaceChild("cube_r371", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6705F, 0.0F, -0.1263F, 0.6765F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 4.0844F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r372 = group26.addOrReplaceChild("cube_r372", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5475F, 0.0F, -0.1263F, 0.5535F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7373F, -1.3848F, 3.6744F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r373 = group26.addOrReplaceChild("cube_r373", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4245F, 0.0F, -0.1263F, 0.4305F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1268F, -1.4053F, 3.8384F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r374 = group26.addOrReplaceChild("cube_r374", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2605F, 0.0F, -0.1263F, 0.3075F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9833F, -1.4053F, 3.2644F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r375 = group26.addOrReplaceChild("cube_r375", CubeListBuilder.create().texOffs(0, 0).addBox(-0.281F, 0.0F, -0.1263F, 0.287F, 0.205F, 0.123F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7578F, -1.3848F, 3.1619F, 0.0F, -0.7854F, 0.0F));

		PartDefinition group27 = group24.addOrReplaceChild("group27", CubeListBuilder.create().texOffs(38, 65).addBox(7.1731F, -2.6963F, 3.5399F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.9283F, -2.6963F, 4.8989F, 0.5535F, 0.205F, 0.205F, new CubeDeformation(0.0F))
		.texOffs(36, 65).addBox(9.0424F, -2.6963F, 3.5399F, 0.205F, 0.205F, 0.7995F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r376 = group27.addOrReplaceChild("cube_r376", CubeListBuilder.create().texOffs(20, 62).addBox(-0.1884F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2474F, -2.6758F, 4.3189F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r377 = group27.addOrReplaceChild("cube_r377", CubeListBuilder.create().texOffs(64, 30).addBox(-0.0165F, 0.0F, 0.0098F, 0.205F, 0.205F, 1.0865F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1731F, -2.6758F, 4.3189F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r378 = group27.addOrReplaceChild("cube_r378", CubeListBuilder.create().texOffs(56, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.6446F, -2.6758F, 2.9249F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r379 = group27.addOrReplaceChild("cube_r379", CubeListBuilder.create().texOffs(44, 65).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1367F, -2.6963F, 2.4534F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r380 = group27.addOrReplaceChild("cube_r380", CubeListBuilder.create().texOffs(0, 0).addBox(-0.205F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1982F, -2.6963F, 2.2689F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r381 = group27.addOrReplaceChild("cube_r381", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2725F, 0.0F, -0.0648F, 0.287F, 0.205F, 0.3075F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2392F, -2.6758F, 2.0434F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r382 = group27.addOrReplaceChild("cube_r382", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1905F, 0.0F, -0.0033F, 0.205F, 0.205F, 0.2255F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3007F, -2.6963F, 2.1664F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r383 = group27.addOrReplaceChild("cube_r383", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.205F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2224F, -2.6963F, 2.2689F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r384 = group27.addOrReplaceChild("cube_r384", CubeListBuilder.create().texOffs(42, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2839F, -2.6963F, 2.4534F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r385 = group27.addOrReplaceChild("cube_r385", CubeListBuilder.create().texOffs(54, 65).addBox(0.0F, 0.0F, 0.0F, 0.205F, 0.205F, 0.7585F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7759F, -2.6758F, 2.9249F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group28 = group24.addOrReplaceChild("group28", CubeListBuilder.create().texOffs(42, 66).addBox(7.308F, -2.6406F, 3.5466F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.965F, -2.6406F, 4.7289F, 0.4815F, 0.1784F, 0.1784F, new CubeDeformation(0.0F))
		.texOffs(66, 42).addBox(8.9342F, -2.6406F, 3.5466F, 0.1784F, 0.1784F, 0.6956F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.95F, -0.075F));

		PartDefinition cube_r386 = group28.addOrReplaceChild("cube_r386", CubeListBuilder.create().texOffs(64, 44).addBox(-0.1639F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1125F, -2.6228F, 4.2243F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r387 = group28.addOrReplaceChild("cube_r387", CubeListBuilder.create().texOffs(48, 64).addBox(-0.0144F, 0.0F, 0.0085F, 0.1784F, 0.1784F, 0.9453F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.308F, -2.6228F, 4.2243F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r388 = group28.addOrReplaceChild("cube_r388", CubeListBuilder.create().texOffs(66, 50).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.7182F, -2.6228F, 3.0115F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r389 = group28.addOrReplaceChild("cube_r389", CubeListBuilder.create().texOffs(52, 66).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1462F, -2.6406F, 2.6013F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r390 = group28.addOrReplaceChild("cube_r390", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1784F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1997F, -2.6406F, 2.4408F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r391 = group28.addOrReplaceChild("cube_r391", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2371F, 0.0F, -0.0564F, 0.2497F, 0.1784F, 0.2675F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2354F, -2.6228F, 2.2446F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r392 = group28.addOrReplaceChild("cube_r392", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1657F, 0.0F, -0.0029F, 0.1784F, 0.1784F, 0.1962F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2889F, -2.6406F, 2.3516F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r393 = group28.addOrReplaceChild("cube_r393", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.1784F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2208F, -2.6406F, 2.4408F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r394 = group28.addOrReplaceChild("cube_r394", CubeListBuilder.create().texOffs(66, 51).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2743F, -2.6406F, 2.6013F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r395 = group28.addOrReplaceChild("cube_r395", CubeListBuilder.create().texOffs(50, 66).addBox(0.0F, 0.0F, 0.0F, 0.1784F, 0.1784F, 0.6599F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7023F, -2.6228F, 3.0115F, 0.0F, 0.3927F, 0.0F));

		PartDefinition group = eq.addOrReplaceChild("group", CubeListBuilder.create().texOffs(64, 27).addBox(7.3742F, -1.4748F, 2.7357F, 1.1562F, 0.1927F, 0.7323F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.1816F, -1.4556F, 2.9862F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(8.527F, -1.4556F, 2.9862F, 0.1927F, 0.1927F, 0.4817F, new CubeDeformation(0.0F))
		.texOffs(48, 63).addBox(7.143F, -1.4748F, 3.468F, 1.6187F, 0.1927F, 0.8093F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.5669F, -1.4748F, 2.543F, 0.7708F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.7596F, -1.4748F, 2.3503F, 0.3854F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.4513F, -1.4556F, 4.47F, 1.002F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.644F, -1.4556F, 4.6627F, 0.6359F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.2586F, -1.4556F, 4.2773F, 1.3874F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.6952F, -1.5327F, 4.7454F, 0.5203F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.8494F, -1.5327F, 4.5527F, 0.212F, 0.1927F, 0.1927F, new CubeDeformation(0.0F))
		.texOffs(66, 2).addBox(6.9854F, -1.5327F, 3.468F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F))
		.texOffs(66, 3).addBox(8.7424F, -1.5327F, 3.468F, 0.1927F, 0.1927F, 0.7515F, new CubeDeformation(0.0F)), PartPose.offset(8.275F, -4.9F, -8.0F));

		PartDefinition cube_r396 = group.addOrReplaceChild("cube_r396", CubeListBuilder.create().texOffs(64, 35).addBox(-0.1771F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9351F, -1.5134F, 4.2002F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r397 = group.addOrReplaceChild("cube_r397", CubeListBuilder.create().texOffs(66, 32).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4919F, -1.5134F, 2.8899F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r398 = group.addOrReplaceChild("cube_r398", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9716F, -1.5327F, 2.2732F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r399 = group.addOrReplaceChild("cube_r399", CubeListBuilder.create().texOffs(34, 66).addBox(0.0F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0294F, -1.5327F, 2.4467F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r400 = group.addOrReplaceChild("cube_r400", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1791F, 0.0F, -0.0031F, 0.1927F, 0.1927F, 0.212F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0452F, -1.5327F, 2.1769F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r401 = group.addOrReplaceChild("cube_r401", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2562F, 0.0F, -0.061F, 0.2698F, 0.1927F, 0.289F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9874F, -1.5134F, 2.0613F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r402 = group.addOrReplaceChild("cube_r402", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.1927F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9489F, -1.5327F, 2.2732F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r403 = group.addOrReplaceChild("cube_r403", CubeListBuilder.create().texOffs(66, 25).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8911F, -1.5327F, 2.4467F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r404 = group.addOrReplaceChild("cube_r404", CubeListBuilder.create().texOffs(38, 66).addBox(-0.1927F, 0.0F, 0.0F, 0.1927F, 0.1927F, 0.713F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4286F, -1.5134F, 2.8899F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r405 = group.addOrReplaceChild("cube_r405", CubeListBuilder.create().texOffs(64, 34).addBox(-0.0156F, 0.0F, 0.0092F, 0.1927F, 0.1927F, 1.0213F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9854F, -1.5134F, 4.2002F, 0.0F, 0.7854F, 0.0F));

		PartDefinition as = liebe2.addOrReplaceChild("as", CubeListBuilder.create(), PartPose.offset(5.0F, 14.0F, 0.0F));

		PartDefinition sa = as.addOrReplaceChild("sa", CubeListBuilder.create().texOffs(64, 62).addBox(-0.425F, -2.1F, -0.375F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(16, 65).addBox(-5.475F, 4.175F, -0.275F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.525F, 2.125F, 4.825F, 0.425F, 0.5F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(46, 66).addBox(-0.525F, 0.0F, 4.925F, 0.5F, 0.55F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(60, 64).addBox(-0.875F, -2.3F, 4.575F, 0.5F, 1.125F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(66, 15).addBox(-5.75F, -2.25F, 4.625F, 0.5F, 0.775F, 0.35F, new CubeDeformation(0.0F))
		.texOffs(62, 64).addBox(-5.75F, -4.75F, 4.475F, 0.5F, 0.775F, 0.45F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r406 = sa.addOrReplaceChild("cube_r406", CubeListBuilder.create().texOffs(34, 19).addBox(-0.5F, -0.25F, 0.0F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.775F, 0.0F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r407 = sa.addOrReplaceChild("cube_r407", CubeListBuilder.create().texOffs(28, 45).addBox(-0.5F, -0.25F, -5.5F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 2.375F, 5.2F, 0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r408 = sa.addOrReplaceChild("cube_r408", CubeListBuilder.create().texOffs(14, 57).addBox(-5.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.125F, 0.3F, 5.3F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r409 = sa.addOrReplaceChild("cube_r409", CubeListBuilder.create().texOffs(48, 57).addBox(-4.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 6.1F, 0.125F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r410 = sa.addOrReplaceChild("cube_r410", CubeListBuilder.create().texOffs(0, 57).addBox(-4.5F, -0.25F, -0.5F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -2.2F, 0.125F, 0.0F, 0.0F, 0.3927F));

		PartDefinition sd = as.addOrReplaceChild("sd", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, 2.3556F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 6.0736F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 4.2036F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r411 = sd.addOrReplaceChild("cube_r411", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r412 = sd.addOrReplaceChild("cube_r412", CubeListBuilder.create().texOffs(0, 52).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r413 = sd.addOrReplaceChild("cube_r413", CubeListBuilder.create().texOffs(48, 18).addBox(2.068F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 11.8376F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		PartDefinition ds = as.addOrReplaceChild("ds", CubeListBuilder.create().texOffs(24, 65).addBox(-5.8F, -0.25F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(64, 64).addBox(-5.7F, 6.025F, 4.5F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 3.975F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(66, 23).addBox(-0.5F, 1.85F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r414 = ds.addOrReplaceChild("cube_r414", CubeListBuilder.create().texOffs(58, 18).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r415 = ds.addOrReplaceChild("cube_r415", CubeListBuilder.create().texOffs(58, 55).addBox(-0.5715F, -5.0276F, 2.05F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r416 = ds.addOrReplaceChild("cube_r416", CubeListBuilder.create().texOffs(40, 26).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r417 = ds.addOrReplaceChild("cube_r417", CubeListBuilder.create().texOffs(0, 38).addBox(-2.85F, -11.0052F, 1.4525F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 10.525F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition df = as.addOrReplaceChild("df", CubeListBuilder.create().texOffs(8, 65).addBox(-5.8F, -2.775F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(20, 65).addBox(-5.7F, 3.5F, 4.5F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 1.45F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(28, 66).addBox(-0.5F, -0.675F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r418 = df.addOrReplaceChild("cube_r418", CubeListBuilder.create().texOffs(58, 53).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.15F, 6.25F, 7.4F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r419 = df.addOrReplaceChild("cube_r419", CubeListBuilder.create().texOffs(58, 20).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r420 = df.addOrReplaceChild("cube_r420", CubeListBuilder.create().texOffs(0, 59).addBox(-0.5715F, -5.0276F, 2.05F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r421 = df.addOrReplaceChild("cube_r421", CubeListBuilder.create().texOffs(40, 40).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r422 = df.addOrReplaceChild("cube_r422", CubeListBuilder.create().texOffs(40, 33).addBox(-2.85F, -3.3369F, -1.7238F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F))
		.texOffs(48, 0).addBox(-2.85F, -11.0052F, 1.4525F, 0.5F, 0.5F, 5.35F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 8.0F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition fd = as.addOrReplaceChild("fd", CubeListBuilder.create().texOffs(12, 65).addBox(-5.8F, 1.575F, -0.55F, 0.5F, 0.6F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.6F, 5.8F, 4.625F, 0.45F, 0.5F, 0.425F, new CubeDeformation(0.0F))
		.texOffs(30, 66).addBox(-0.5F, 3.675F, -0.45F, 0.35F, 0.55F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r423 = fd.addOrReplaceChild("cube_r423", CubeListBuilder.create().texOffs(54, 45).addBox(-6.2856F, -8.9574F, -2.95F, 5.5F, 0.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r424 = fd.addOrReplaceChild("cube_r424", CubeListBuilder.create().texOffs(14, 45).addBox(2.325F, -6.9064F, -5.8149F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r425 = fd.addOrReplaceChild("cube_r425", CubeListBuilder.create().texOffs(0, 45).addBox(-2.85F, -11.0051F, 1.4525F, 0.5F, 0.5F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, 12.35F, 2.475F, 0.3927F, 0.0F, 0.0F));

		PartDefinition fg = as.addOrReplaceChild("fg", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, 0.1556F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.9715F, 5.6776F, 4.3822F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 3.8736F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 2.0036F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r426 = fg.addOrReplaceChild("cube_r426", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r427 = fg.addOrReplaceChild("cube_r427", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r428 = fg.addOrReplaceChild("cube_r428", CubeListBuilder.create().texOffs(50, 48).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r429 = fg.addOrReplaceChild("cube_r429", CubeListBuilder.create().texOffs(40, 48).addBox(2.068F, -2.9365F, -1.5169F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F))
		.texOffs(14, 38).addBox(2.068F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 9.6376F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		PartDefinition gf = as.addOrReplaceChild("gf", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8835F, -2.4944F, -0.0617F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.9715F, 3.0276F, 4.3822F, 0.44F, 0.528F, 0.44F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, 1.2236F, 4.4923F, 0.396F, 0.44F, 0.374F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.4155F, -0.6464F, 0.0263F, 0.308F, 0.484F, 0.44F, new CubeDeformation(0.0F)), PartPose.offset(12.85F, -11.575F, -2.375F));

		PartDefinition cube_r430 = gf.addOrReplaceChild("cube_r430", CubeListBuilder.create().texOffs(0, 0).addBox(0.6914F, -7.8825F, -2.596F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r431 = gf.addOrReplaceChild("cube_r431", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 3.8876F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r432 = gf.addOrReplaceChild("cube_r432", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3371F, -4.4243F, 1.804F, 4.84F, 0.44F, 0.44F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r433 = gf.addOrReplaceChild("cube_r433", CubeListBuilder.create().texOffs(10, 52).addBox(-2.486F, -6.0776F, -5.1171F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r434 = gf.addOrReplaceChild("cube_r434", CubeListBuilder.create().texOffs(28, 51).addBox(2.068F, -2.9365F, -1.5169F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F))
		.texOffs(38, 53).addBox(2.043F, -9.6845F, 1.2782F, 0.44F, 0.44F, 4.84F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9515F, 6.9876F, 2.6002F, 0.3927F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		liebe.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		liebe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}